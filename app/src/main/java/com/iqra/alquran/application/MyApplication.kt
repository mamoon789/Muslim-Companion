package com.iqra.alquran.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.views.LoadingFragment
import com.iqra.alquran.views.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


//class
//MyApplication : Application(), Application.ActivityLifecycleCallbacks,
//    LifecycleEventObserver
//{
//    private lateinit var appOpenAdManager: AppOpenAdManager
//    private var currentActivity: Activity? = null
//    val event = MutableLiveData(1)
//
//    override fun onCreate()
//    {
//        super.onCreate()
//        registerActivityLifecycleCallbacks(this)
//        MobileAds.initialize(this) {}
//        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//        appOpenAdManager = AppOpenAdManager()
//    }
//
//    /** ActivityLifecycleCallback methods. */
//    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?)
//    {
//    }
//
//    override fun onActivityStarted(activity: Activity)
//    {
//        // Updating the currentActivity only when an ad is not showing.
//        if (!appOpenAdManager.isShowingAd)
//        {
//            currentActivity = activity
//        }
//    }
//
//    override fun onActivityResumed(activity: Activity)
//    {
//    }
//
//    override fun onActivityPaused(activity: Activity)
//    {
//    }
//
//    override fun onActivityStopped(activity: Activity)
//    {
//    }
//
//    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle)
//    {
//    }
//
//    override fun onActivityDestroyed(activity: Activity)
//    {
//    }
//
//    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event)
//    {
//        if (event == Lifecycle.Event.ON_START)
//        {
//            onMoveToForeground()
//        }
//    }
//
//    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
//    private fun onMoveToForeground()
//    {
//        // Show the ad (if available) when the app moves to foreground.
//        currentActivity?.let {
//            val sharedPreferences = getSharedPreferences(
//                getString(R.string.settings),
//                Context.MODE_PRIVATE
//            )
//            if (!sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false))
//                appOpenAdManager.showAdIfAvailable(it)
//        }
//    }
//
//    /** Interface definition for a callback to be invoked when an app open ad is complete. */
//    interface OnShowAdCompleteListener
//    {
//        fun onShowAdComplete()
//    }
//
//    private inner class AppOpenAdManager
//    {
//        private var appOpenAd: AppOpenAd? = null
//        private var isLoadingAd = false
//        var isShowingAd = false
//
//        /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
//        private var loadTime: Long = 0
//
//        /** Show the ad if one isn't already showing. */
//        fun showAdIfAvailable(activity: Activity)
//        {
//            showAdIfAvailable(
//                activity,
//                object : OnShowAdCompleteListener
//                {
//                    override fun onShowAdComplete()
//                    {
//                        // Empty because the user will go back to the activity that shows the ad.
//                    }
//                })
//        }
//
//        /** Shows the ad if one isn't already showing. */
//        fun showAdIfAvailable(
//            activity: Activity,
//            onShowAdCompleteListener: OnShowAdCompleteListener
//        )
//        {
//            // If the app open ad is already showing, do not show the ad again.
//            if (isShowingAd || Constants.INTERSTITIAL_AD_SHOWN)
//            {
//                Log.d("LOG_TAG", "The app open ad is already showing.")
//                return
//            }
//
//            // If the app open ad is not available yet, invoke the callback then load the ad.
//            if (!isAdAvailable())
//            {
//                Log.d("LOG_TAG", "The app open ad is not ready yet.")
//                onShowAdCompleteListener.onShowAdComplete()
//                loadAd(activity)
//                return
//            }
//
//            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback()
//            {
//
//                override fun onAdDismissedFullScreenContent()
//                {
//                    // Called when full screen content is dismissed.
//                    // Set the reference to null so isAdAvailable() returns false.
//                    Log.d("LOG_TAG", "Ad dismissed fullscreen content.")
//                    appOpenAd = null
//                    isShowingAd = false
//
//                    onShowAdCompleteListener.onShowAdComplete()
//                    loadAd(activity)
//                }
//
//                override fun onAdFailedToShowFullScreenContent(adError: AdError)
//                {
//                    // Called when fullscreen content failed to show.
//                    // Set the reference to null so isAdAvailable() returns false.
//                    Log.d("LOG_TAG", adError.message)
//                    appOpenAd = null
//                    isShowingAd = false
//
//                    onShowAdCompleteListener.onShowAdComplete()
//                    loadAd(activity)
//                }
//
//                override fun onAdShowedFullScreenContent()
//                {
//                    // Called when fullscreen content is shown.
//                    Log.d("LOG_TAG", "Ad showed fullscreen content.")
//                }
//            }
//            isShowingAd = true
//            appOpenAd?.show(activity)
//        }
//
//        /** Request an ad. */
//        fun loadAd(activity: Activity)
//        {
//            // Do not load ad if there is an unused ad or one is already loading.
//            if (isLoadingAd || isAdAvailable())
//            {
//                return
//            }
//
//            isLoadingAd = true
//            val request = AdRequest.Builder().build()
//            AppOpenAd.load(
//                activity, Constants.APPOPEN_AD_ID, request,
//                object : AppOpenAd.AppOpenAdLoadCallback()
//                {
//
//                    override fun onAdLoaded(ad: AppOpenAd)
//                    {
//                        // Called when an app open ad has loaded.
//                        Log.d("LOG_TAG", "Ad was loaded.")
//                        appOpenAd = ad
//                        isLoadingAd = false
//                        loadTime = Date().time
//                        appOpenAd?.show(activity)
//                    }
//
//                    override fun onAdFailedToLoad(loadAdError: LoadAdError)
//                    {
//                        // Called when an app open ad has failed to load.
//                        Log.d("LOG_TAG", loadAdError.message)
//                        isLoadingAd = false;
//                    }
//                })
//        }
//
//        /** Check if ad exists and can be shown. */
//        private fun isAdAvailable(): Boolean
//        {
//            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
//        }
//
//        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean
//        {
//            val dateDifference: Long = Date().time - loadTime
//            val numMilliSecondsPerHour: Long = 3600000
//            return dateDifference < numMilliSecondsPerHour * numHours
//        }
//    }
//}


class
MyApplication : Application()
{
    private lateinit var activityLifecycleCallbacks: ActivityLifecycleCallbacks
    private lateinit var lifecycleEventObserver: LifecycleEventObserver
    lateinit var appOpenAdManager: AppOpenAdManager
    private lateinit var currentActivity: MainActivity
    val event = MutableLiveData(-1)
    var isActivityInit = false

    override fun onCreate()
    {
        super.onCreate()
        activityLifecycleCallbacks = object : ActivityLifecycleCallbacks
        {
            override fun onActivityCreated(p0: Activity, p1: Bundle?)
            {
            }

            override fun onActivityStarted(activity: Activity)
            {
                currentActivity = if (!isActivityInit) activity as MainActivity else return
                isActivityInit = true
            }

            override fun onActivityResumed(p0: Activity)
            {
            }

            override fun onActivityPaused(p0: Activity)
            {
            }

            override fun onActivityStopped(p0: Activity)
            {
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle)
            {
            }

            override fun onActivityDestroyed(p0: Activity)
            {
            }

        }
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        lifecycleEventObserver = LifecycleEventObserver { source, eventt ->
            if (eventt == Lifecycle.Event.ON_START)
            {
                if (Constants.INTERSTITIAL_AD_SHOWN)
                {
                    return@LifecycleEventObserver
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val sharedPreferences = getSharedPreferences(
                        getString(R.string.settings),
                        Context.MODE_PRIVATE
                    )
                    val isSubscribed =
                        sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false)
                    if (isSubscribed)
                    {
                        return@launch
                    }

                    var fragment =
                        currentActivity.supportFragmentManager.findFragmentByTag("mecca")
                    if (fragment != null) return@launch

                    fragment = currentActivity.supportFragmentManager.findFragmentByTag("medina")
                    if (fragment != null) return@launch

                    fragment =
                        currentActivity.supportFragmentManager.findFragmentByTag("filter-fragment")
                    if (fragment != null) return@launch

                    fragment = currentActivity.supportFragmentManager.findFragmentByTag("splash")
                    if (fragment == null)
                    {
                        currentActivity.supportFragmentManager.beginTransaction()
                            .replace(R.id.container, LoadingFragment.newInstance(), "loading")
                            .addToBackStack(null)
                            .commit()

                        delay(500)
                    }

                    appOpenAdManager.showAdIfAvailable(currentActivity)
                }
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)

        MobileAds.initialize(this) {}
        appOpenAdManager = AppOpenAdManager()
    }

    /** Interface definition for a callback to be invoked when an app open ad is complete. */
    interface OnAdCompleteListener
    {
        fun onShowAdComplete()
        fun onLoadAdComplete()
    }

    inner class AppOpenAdManager
    {
        private lateinit var activity: Activity
        private val appOpenAd = MutableLiveData<AppOpenAd?>(null)
        private var isLoadingAd = false
        var isShowingAd = false

        /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
        private var loadTime: Long = 0
        private val onAdCompleteListener = object : OnAdCompleteListener
        {
            override fun onShowAdComplete()
            {
                event.postValue(1)
            }

            override fun onLoadAdComplete()
            {
                event.postValue(0)
            }
        }

        init
        {
            appOpenAd.observe(ProcessLifecycleOwner.get()) { ad ->
                ad?.fullScreenContentCallback =
                    object : FullScreenContentCallback()
                    {

                        override fun onAdDismissedFullScreenContent()
                        {
                            // Called when full screen content is dismissed.
                            // Set the reference to null so isAdAvailable() returns false.
                            Log.d("LOG_TAG", "Ad dismissed fullscreen content.")
                            appOpenAd.value = null
                            isShowingAd = false

                            onAdCompleteListener.onShowAdComplete()
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError)
                        {
                            // Called when fullscreen content failed to show.
                            // Set the reference to null so isAdAvailable() returns false.
                            Log.d("LOG_TAG", adError.message)
                            appOpenAd.value = null
                            isShowingAd = false

                            onAdCompleteListener.onShowAdComplete()
                            loadAd()
                        }

                        override fun onAdShowedFullScreenContent()
                        {
                            // Called when fullscreen content is shown.
                            Log.d("LOG_TAG", "Ad showed fullscreen content.")
                        }
                    }
            }

        }

        /** Shows the ad if one isn't already showing. */
        fun showAdIfAvailable(activityy: Activity)
        {
            activity = activityy

            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd)
            {
                Log.d("LOG_TAG", "The app open ad is already showing.")
                return
            }

            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable())
            {
                Log.d("LOG_TAG", "The app open ad is not ready yet.")
                loadAd(
                    loadCallback = { onAdCompleteListener.onLoadAdComplete() },
                    showCallback = { onAdCompleteListener.onShowAdComplete() }
                )
                return
            }

            isShowingAd = true
            appOpenAd.value?.show(activity)
        }

        fun loadAd(loadCallback: (() -> Unit)? = null, showCallback: (() -> Unit)? = null)
        {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable())
            {
                return
            }

            isLoadingAd = true

            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                activity,
                if (BuildConfig.DEBUG) Constants.APPOPEN_AD_ID else BuildConfig.APPOPEN_AD_ID,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback()
                {

                    override fun onAdLoaded(ad: AppOpenAd)
                    {
                        Log.d("LOG_TAG", "Ad was loaded.")
                        appOpenAd.postValue(ad)
                        isLoadingAd = false
                        loadTime = Date().time
                        loadCallback?.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError)
                    {
                        Log.d("LOG_TAG", loadAdError.message)
                        appOpenAd.postValue(null)
                        isLoadingAd = false;
                        showCallback?.invoke()
                    }
                })
        }

        /** Check if ad exists and can be shown. */
        private fun isAdAvailable(): Boolean
        {
            return appOpenAd.value != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean
        {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }
    }
}