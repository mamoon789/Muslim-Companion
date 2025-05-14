package com.iqra.alquran.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.*
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.work.*
import com.android.billingclient.api.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Quran
import com.iqra.alquran.utils.Billing
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.utils.Utility
import com.iqra.alquran.worker.AlarmWorker
import com.yarolegovich.slidingrootnav.R.string.srn_drawer_close
import com.yarolegovich.slidingrootnav.R.string.srn_drawer_open
import com.yarolegovich.slidingrootnav.SlideGravity
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import com.yarolegovich.slidingrootnav.util.ActionBarToggleAdapter
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), View.OnClickListener
{
    lateinit var sharedPreferences: SharedPreferences

    lateinit var toolbar: Toolbar
    lateinit var slidingRootNav: SlidingRootNav
    lateinit var quran: TextView
    private lateinit var hadith: TextView
    private lateinit var prayer: TextView
    private lateinit var zakat: TextView
    private lateinit var tasbeeh: TextView
    private lateinit var qibla: TextView
    private lateinit var mecca: TextView
    private lateinit var medina: TextView
    private lateinit var asmaAlHusna: TextView
    private lateinit var goPremium: TextView
    private lateinit var goPremiumMsg: TextView
    private lateinit var share: TextView
    private lateinit var review: TextView

    lateinit var surahs: MutableList<Quran.Data.Surah>

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var reviewManager: ReviewManager
    private var reviewInfo: ReviewInfo? = null

    private var progressDialog: AlertDialog? = null
    private var interstitialAd: InterstitialAd? = null

    private lateinit var billing: Billing

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot)
        {
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        slidingRootNav = SlidingRootNavBuilder(this)
            .withToolbarMenuToggle(toolbar)
            .withMenuOpened(false)
            .withContentClickableWhenMenuOpened(false)
            .withSavedState(savedInstanceState)
            .withMenuLayout(R.layout.menu_left_drawer)
            .withGravity(if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) SlideGravity.RIGHT else SlideGravity.LEFT)
            .inject()

        ActionBarToggleAdapter(this).run {
            setAdaptee(slidingRootNav.layout)
            ActionBarDrawerToggle(
                this@MainActivity,
                this,
                toolbar,
                srn_drawer_open,
                srn_drawer_close
            ).run {
                syncState()
                isDrawerIndicatorEnabled = false
                setToolbarNavigationClickListener {
                    drawerListener()
                }
            }
        }

        quran = findViewById(R.id.quran)
        hadith = findViewById(R.id.hadith)
        prayer = findViewById(R.id.prayer)
        zakat = findViewById(R.id.zakat)
        tasbeeh = findViewById(R.id.tasbeeh)
        qibla = findViewById(R.id.qibla)
        mecca = findViewById(R.id.mecca)
        medina = findViewById(R.id.medina)
        asmaAlHusna = findViewById(R.id.asmaAlHusna)
        goPremium = findViewById(R.id.goPremium)
        goPremiumMsg = findViewById(R.id.goPremiumMsg)
        share = findViewById(R.id.share)
        review = findViewById(R.id.review)

        goPremium.setOnClickListener(this)
        goPremiumMsg.setOnClickListener(this)
        quran.setOnClickListener(this)
        hadith.setOnClickListener(this)
        prayer.setOnClickListener(this)
        zakat.setOnClickListener(this)
        tasbeeh.setOnClickListener(this)
        qibla.setOnClickListener(this)
        mecca.setOnClickListener(this)
        medina.setOnClickListener(this)
        asmaAlHusna.setOnClickListener(this)
        share.setOnClickListener(this)
        review.setOnClickListener(this)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        reviewManager = ReviewManagerFactory.create(this)
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful)
            {
                reviewInfo = task.result
            }
        }

        sharedPreferences = getSharedPreferences(
            getString(R.string.settings),
            Context.MODE_PRIVATE
        )

        firebaseAnalytics = Firebase.analytics

        billing = Billing(this)

        surahs = Utility.getQuran(this).data.surahs

//        Utility.makeHtmlBodyWithTranslation(this)

        AlarmWorker.updateAlarms(this, false)

        showFragment(SplashFragment.newInstance(), "splash")
    }

    override fun onResume()
    {
        super.onResume()
        installInAppUpdate()
        billing.checkSubPurchase()
    }

    private fun installInAppUpdate()
    {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            )
            {
                try
                {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        this,
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                        100,
                    )
                } catch (e: SendIntentException)
                {
                    e.printStackTrace()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100)
        {
            if (resultCode == RESULT_CANCELED)
            {
                Log.e("AppUpdate", "Update flow failed! Result code: $resultCode")
                exitProcess(0)
            } else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED)
            {
                Log.e("AppUpdate", "Update flow failed! Result code: $resultCode")
                installInAppUpdate()
            }
        }
    }

    private fun drawerListener()
    {
        if (slidingRootNav.isMenuClosed)
        {
            if (reviewInfo != null)
            {
                reviewManager.launchReviewFlow(this, reviewInfo!!)
                    .addOnCompleteListener {
                        slidingRootNav.openMenu(true)
                    }
            } else
            {
                slidingRootNav.openMenu(true)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed()
    {
        if (Constants.INTERSTITIAL_AD_SHOWN)
        {
            return
        }
        if (supportFragmentManager.backStackEntryCount == 0)
        {
            supportFragmentManager.findFragmentByTag("splash")?.apply {
                if (isVisible) return
            } ?: run {
                showCustomDialog()
            }
        } else
        {
            supportFragmentManager.findFragmentByTag("loading")?.apply {
                if (isVisible) return
            } ?: run {
                super.onBackPressed()
            }
        }
    }

    override fun onClick(v: View)
    {
        when (v.id)
        {
            R.id.quran ->
            {
                firebaseAnalytics.logEvent("quran_frag", null)
                updateToolbar(title = resources.getString(R.string.app_name))
                updateSideMenu(tv = quran)
                showFragment(QuranNavFragment.newInstance())
            }

            R.id.hadith ->
            {
                if (checkInternetConnection())
                {
                    firebaseAnalytics.logEvent("hadith_frag", null)
                    updateToolbar(title = resources.getString(R.string.hadith))
                    updateSideMenu(tv = hadith)
                    showFragment(HadithNavFragment.newInstance())
                }
            }

            R.id.prayer ->
            {
                if (checkInternetConnection())
                {
                    if (checkPermission())
                    {
                        firebaseAnalytics.logEvent("prayer_frag", null)
                        updateToolbar(title = resources.getString(R.string.prayer_times))
                        updateSideMenu(tv = prayer)
                        showFragment(NamazFragment.newInstance())
                    } else
                    {
                        requestPermission(requestCode = R.id.prayer)
                    }
                }
            }

            R.id.zakat ->
            {
                if (checkInternetConnection())
                {
                    firebaseAnalytics.logEvent("zakat_frag", null)
                    updateToolbar(title = resources.getString(R.string.zakat))
                    updateSideMenu(tv = zakat)
                    showFragment(ZakatFragment.newInstance())
                }
            }

            R.id.tasbeeh ->
            {
                if (checkInternetConnection())
                {
                    firebaseAnalytics.logEvent("tasbeeh_frag", null)
                    updateToolbar(title = resources.getString(R.string.tasbeeh))
                    updateSideMenu(tv = tasbeeh)
                    showFragment(TasbeehNavFragment.newInstance())
                }
            }

            R.id.qibla ->
            {
                if (checkInternetConnection())
                {
                    if (checkPermission())
                    {
                        firebaseAnalytics.logEvent("qibla_frag", null)
                        updateToolbar(title = resources.getString(R.string.qibla))
                        updateSideMenu(tv = qibla)
                        showFragment(QiblaFragment.newInstance())
                    } else
                    {
                        requestPermission(requestCode = R.id.qibla)
                    }
                }
            }

            R.id.mecca ->
            {
                if (checkInternetConnection())
                {
                    firebaseAnalytics.logEvent("mecca_frag", null)
                    updateToolbar(title = resources.getString(R.string.live_mecca))
                    updateSideMenu(tv = mecca)
                    showFragment(YoutubeFragment.newInstance(Constants.VIDEO_ID_MECCA), "mecca")
                }
            }

            R.id.medina ->
            {
                if (checkInternetConnection())
                {
                    firebaseAnalytics.logEvent("medina_frag", null)
                    updateToolbar(title = resources.getString(R.string.live_medina))
                    updateSideMenu(tv = medina)
                    showFragment(YoutubeFragment.newInstance(Constants.VIDEO_ID_MEDINA), "medina")
                }
            }

            R.id.asmaAlHusna ->
            {
                if (checkInternetConnection())
                {
                    firebaseAnalytics.logEvent("names_frag", null)
                    updateToolbar(title = resources.getString(R.string.asmaalhusna))
                    updateSideMenu(tv = asmaAlHusna)
                    showFragment(AsmaAlHusnaFragment.newInstance())
                }
            }

            R.id.share ->
            {
                firebaseAnalytics.logEvent("share_app", null)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_data))
                }
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.share)
                    )
                )
            }

            R.id.review ->
            {
                if (checkInternetConnection())
                {
                    firebaseAnalytics.logEvent("review_app", null)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(getString(R.string.store_url))
                    }
                    startActivity(intent)
                }
            }

            R.id.goPremium, R.id.goPremiumMsg ->
            {
                firebaseAnalytics.logEvent("buy_subscription_drawer", null)
                buySubscription()
            }
        }
        slidingRootNav.closeMenu()
    }

    private fun buySubscription(callback: (() -> Unit)? = null)
    {
        try
        {
            billing.launchPurchaseFlow(callback)
        } catch (e: Exception)
        {
            Log.e("billing", "onClick: " + e.message)
            callback?.invoke()
        }
    }

    private fun updateToolbar(title: String, subTitle: String = "")
    {
        toolbar.title = title
        toolbar.subtitle = subTitle
    }

    private fun updateSideMenu(tv: TextView)
    {
        quran.setTextColor(getColor(R.color.black))
        hadith.setTextColor(getColor(R.color.black))
        prayer.setTextColor(getColor(R.color.black))
        zakat.setTextColor(getColor(R.color.black))
        tasbeeh.setTextColor(getColor(R.color.black))
        qibla.setTextColor(getColor(R.color.black))
        mecca.setTextColor(getColor(R.color.black))
        medina.setTextColor(getColor(R.color.black))
        asmaAlHusna.setTextColor(getColor(R.color.black))
        tv.setTextColor(getColor(R.color.purple))

        quran.setTypeface(null, Typeface.NORMAL);
        hadith.setTypeface(null, Typeface.NORMAL);
        prayer.setTypeface(null, Typeface.NORMAL);
        zakat.setTypeface(null, Typeface.NORMAL);
        tasbeeh.setTypeface(null, Typeface.NORMAL);
        qibla.setTypeface(null, Typeface.NORMAL);
        mecca.setTypeface(null, Typeface.NORMAL);
        medina.setTypeface(null, Typeface.NORMAL);
        asmaAlHusna.setTypeface(null, Typeface.NORMAL);
        tv.setTypeface(null, Typeface.BOLD);
    }

    fun loadBanner(adContainer: FrameLayout)
    {
        val isSubscribed = sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false)
        if (isSubscribed)
        {
            return
        }

        val adView = AdView(this)
        adView.adUnitId = if (BuildConfig.DEBUG) Constants.BANNER_AD_ID else BuildConfig.BANNER_AD_ID
        adView.setAdSize(Utility.getBannerAdSize(this))

        adContainer.removeAllViews()
        adContainer.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadAd(callback: (() -> Unit)?)
    {
        val adRequest = AdRequest.Builder().build()

        showCustomDialog(R.layout.dialog_progress_ad)

        InterstitialAd.load(
            this,
            if (BuildConfig.DEBUG) Constants.INTERSTITIAL_AD_ID else BuildConfig.INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback()
            {
                override fun onAdFailedToLoad(adError: LoadAdError)
                {
                    hideDialog()
                    interstitialAd = null
                    callback?.invoke()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd)
                {
                    hideDialog()
                    this@MainActivity.interstitialAd = interstitialAd

                    this@MainActivity.interstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback()
                        {
                            override fun onAdDismissedFullScreenContent()
                            {
                                Constants.INTERSTITIAL_AD_SHOWN = false
                                callback?.invoke()
                            }

                            override fun onAdShowedFullScreenContent()
                            {
                                Constants.INTERSTITIAL_AD_SHOWN = true
                            }
                        }

                    this@MainActivity.interstitialAd?.show(this@MainActivity)
                }
            })
    }

    fun showPremiumDialogOrAd(callback: (() -> Unit)? = null)
    {
        val isSubscribed = sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false)
        if (isSubscribed)
        {
            callback?.invoke()
            return
        }

        val showPremiumDialog = sharedPreferences.getInt(Constants.KEY_SHOW_PREMIUM_DIALOG, 1)
        sharedPreferences.edit()
            .putInt(Constants.KEY_SHOW_PREMIUM_DIALOG, showPremiumDialog + 1)
            .apply()
        if (showPremiumDialog % 6 == 0)
        {
            showCustomDialog(R.layout.dialog_premium2, callback)
            return
        }
        if (showPremiumDialog % 3 == 0)
        {
            showCustomDialog(R.layout.dialog_premium, callback)
            return
        }

        if (!checkInternetConnection(showAlert = false))
        {
            callback?.invoke()
            return
        }

        loadAd(callback)
    }

    fun showCustomDialog(
        layout: Int,
        callback: (() -> Unit)?
    )
    {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(layout, null, false).apply {
            findViewById<Button>(R.id.subscribe).setOnClickListener {
                hideDialog()
                buySubscription(callback)
                firebaseAnalytics.logEvent("buy_subscription_dialog", null)
            }
            findViewById<Button>(R.id.close).setOnClickListener {
                hideDialog()
                if (!checkInternetConnection(showAlert = false))
                {
                    callback?.invoke()
                    return@setOnClickListener
                }
                loadAd(callback)
            }
        }

        progressDialog = AlertDialog.Builder(this).setView(view).create()
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    fun showCustomDialog(
        layout: Any = -1,
        title: String = getString(R.string.exit),
        message: String = getString(R.string.msg_exit),
        positiveTxt: String = getString(R.string.yes),
        negativeTxt: String = getString(R.string.no),
        positiveListener: OnClickListener = OnClickListener { _, _ -> exitProcess(0) },
        negativeListener: OnClickListener = OnClickListener { _, _ -> hideDialog() },
    )
    {
        if (progressDialog == null)
        {
            when
            {
                layout is Int && layout == -1 ->
                {
                    progressDialog = AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(positiveTxt, positiveListener)
                        .setNegativeButton(negativeTxt, negativeListener)
                        .create()
                }

                layout is Int ->
                {
                    progressDialog = AlertDialog.Builder(this).setView(layout).create()
                }

                layout is View ->
                {
                    progressDialog = AlertDialog.Builder(this).setView(layout).create()
                }
            }
        }
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    fun hideDialog()
    {
        if (progressDialog != null && progressDialog!!.isShowing)
        {
            progressDialog!!.dismiss()
            progressDialog = null
        }
    }

    fun checkInternetConnection(showAlert: Boolean = true): Boolean
    {
        val connectionManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConnection = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileDataConnection = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiConnection!!.isConnectedOrConnecting || mobileDataConnection!!.isConnectedOrConnecting)
        {
            return true
        }
        if (showAlert)
        {
            showSnackBar(getString(R.string.msg_connect_internet), true)
        }
        return false
    }

    fun checkPermission(): Boolean
    {
        val coarsePermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val finePermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return coarsePermission == PackageManager.PERMISSION_GRANTED && finePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(requestCode: Int)
    {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty())
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                onClick(findViewById(requestCode))
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                showSnackBar(getString(R.string.msg_permission_require), false)
            } else
            {
                showSnackBar(getString(R.string.msg_permission_require), true)
            }
        }
    }

    fun showSnackBar(
        msg: String,
        action: Boolean
    )
    {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
        if (action)
        {
            snackBar.setAction(getString(R.string.settings)) {
                if (msg == getString(R.string.msg_connect_internet))
                {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                } else
                {
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${packageName}")
                    )
                }.apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(this)
                }
            }
        }
        snackBar.show()
    }

    private fun showFragment(fragment: Fragment, tag: String = "")
    {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, tag)
        transaction.commitAllowingStateLoss()
    }
}