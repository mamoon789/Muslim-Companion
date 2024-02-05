package com.iqra.alquran.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.iqra.alquran.views.MainActivity

class Billing(val activity: MainActivity)
{
    private var billingClient: BillingClient
    var isBillingClientReady = false

    init
    {
        billingClient = BillingClient.newBuilder(activity).enablePendingPurchases()
            .setListener { billingResult, purchaseList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList != null)
                {
                    for (purchase in purchaseList)
                    {
                        verifySubPurchase(purchase)
                    }
                }
            }.build()
        //start the connection after initializing the billing client
        establishConnection()
    }

    fun establishConnection()
    {
        billingClient.startConnection(object : BillingClientStateListener
        {
            override fun onBillingSetupFinished(billingResult: BillingResult)
            {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                {
                    isBillingClientReady = true
                    checkSubPurchase()
                }
            }

            override fun onBillingServiceDisconnected()
            {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                isBillingClientReady = false
                establishConnection()
            }
        })
    }

    fun launchPurchaseFlow()
    {
        if (!isBillingClientReady) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(Constants.PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS).build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
            for (productDetails in productDetailsList)
            {
                if (productDetails.productId == Constants.PRODUCT_ID)
                {
                    assert(productDetails.subscriptionOfferDetails != null)
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList).build()

                    billingClient.launchBillingFlow(activity, billingFlowParams)
                }
            }
        }
    }

    fun checkSubPurchase()
    {
        if (!isBillingClientReady) return

        val param =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()

        billingClient.queryPurchasesAsync(param) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            {
                if (purchaseList.isEmpty())
                {
                    activity.sharedPreferences.edit().putBoolean(Constants.KEY_IS_SUBSCRIBED, false)
                        .apply()
                    return@queryPurchasesAsync
                }

                for (purchase in purchaseList)
                {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED)
                    {
                        if (purchase.isAcknowledged)
                        {
                            activity.sharedPreferences.edit()
                                .putBoolean(Constants.KEY_IS_SUBSCRIBED, true).apply()
                        } else
                        {
                            verifySubPurchase(purchase)
                        }
                    }
                }
            }
        }
    }

    private fun verifySubPurchase(purchase: Purchase)
    {
        val acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            {
                Toast.makeText(activity, "You are a premium user now", Toast.LENGTH_SHORT).show()
                activity.sharedPreferences.edit().putBoolean(Constants.KEY_IS_SUBSCRIBED, true)
                    .apply()
            }
        }

        Log.d("TAG", "Purchase Token: " + purchase.purchaseToken)
        Log.d("TAG", "Purchase Time: " + purchase.purchaseTime)
        Log.d("TAG", "Purchase OrderID: " + purchase.orderId)
    }
}