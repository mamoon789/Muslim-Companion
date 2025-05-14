package com.iqra.alquran.utils

import android.util.Log
import com.android.billingclient.api.*
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.views.MainActivity

class Billing(val activity: MainActivity)
{
    private var billingClient: BillingClient
    var isBillingClientReady = false
    var callback: (() -> Unit)? = null

    init
    {
        billingClient = BillingClient.newBuilder(activity).enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
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

    fun launchPurchaseFlow(callback: (() -> Unit)?)
    {
        this.callback = callback
        if (!isBillingClientReady) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(BuildConfig.PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS).build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
            for (productDetails in productDetailsList)
            {
                if (productDetails.productId == BuildConfig.PRODUCT_ID)
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
       callback = callback?.run {
            invoke()
            null
        }
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
                    } else
                    {
                        activity.sharedPreferences.edit()
                            .putBoolean(Constants.KEY_IS_SUBSCRIBED, false)
                            .apply()
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
                activity.sharedPreferences.edit().putBoolean(Constants.KEY_IS_SUBSCRIBED, true)
                    .apply()
            }
        }

        Log.d("TAG", "Purchase Token: " + purchase.purchaseToken)
        Log.d("TAG", "Purchase Time: " + purchase.purchaseTime)
        Log.d("TAG", "Purchase OrderID: " + purchase.orderId)
    }
}