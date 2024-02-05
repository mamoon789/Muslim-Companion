package com.iqra.alquran.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX
import com.google.gson.Gson
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Quran
import com.iqra.alquran.utils.Billing
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.worker.AlarmWorker
import com.yarolegovich.slidingrootnav.SlideGravity
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import com.yarolegovich.slidingrootnav.util.ActionBarToggleAdapter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), View.OnClickListener
{
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var toolbar: Toolbar
    lateinit var slidingRootNav: SlidingRootNav
    lateinit var quran: TextView
    private lateinit var prayer: TextView
    private lateinit var setting: TextView
    private lateinit var mosque: TextView
    private lateinit var zakat: TextView
    private lateinit var qibla: TextView
    private lateinit var mecca: TextView
    private lateinit var medina: TextView
    private lateinit var asmaAlHusna: TextView
    private lateinit var removeAds: TextView
    private lateinit var share: TextView
    private lateinit var review: TextView
    private lateinit var lastView: View

    lateinit var surahs: MutableList<Quran.Data.Surah>
    lateinit var language: String

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var reviewManager: ReviewManager
    private var reviewInfo: ReviewInfo? = null

    private var progressDialog: AlertDialog? = null
    private var interstitialAd: InterstitialAd? = null

    lateinit var billing: Billing

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
                R.string.srn_drawer_open,
                R.string.srn_drawer_close
            ).run {
                syncState()
                isDrawerIndicatorEnabled = false
                setToolbarNavigationClickListener {
                    onBackPressed()
                }
            }
        }

        quran = findViewById(R.id.quran)
        prayer = findViewById(R.id.prayer)
        setting = findViewById(R.id.settings)
        mosque = findViewById(R.id.mosque)
        zakat = findViewById(R.id.zakat)
        qibla = findViewById(R.id.qibla)
        mecca = findViewById(R.id.mecca)
        medina = findViewById(R.id.medina)
        asmaAlHusna = findViewById(R.id.asmaAlHusna)
        removeAds = findViewById(R.id.removeAds)
        share = findViewById(R.id.share)
        review = findViewById(R.id.review)

        removeAds.setOnClickListener(this)
        quran.setOnClickListener(this)
        prayer.setOnClickListener(this)
        setting.setOnClickListener(this)
        mosque.setOnClickListener(this)
        zakat.setOnClickListener(this)
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

        billing = Billing(this)

        val quran = getQuran()
        surahs = quran.data.surahs
        language = quran.data.edition.language

        AlarmWorker.updateAlarms(this, false)

//        if (savedInstanceState == null)
//        {
            showFragment(SplashFragment.newInstance())
//        } else
//        {
//            val meccaFragment = supportFragmentManager.findFragmentByTag(Constants.VIDEO_ID_MECCA)
//            val medinaFragment = supportFragmentManager.findFragmentByTag(Constants.VIDEO_ID_MEDINA)
//            if (meccaFragment != null)
//            {
//                showYoutubeFragment(
//                    meccaFragment as YouTubePlayerSupportFragmentX,
//                    Constants.VIDEO_ID_MECCA,
//                    false
//                )
//            } else if (medinaFragment != null)
//            {
//                showYoutubeFragment(
//                    medinaFragment as YouTubePlayerSupportFragmentX,
//                    Constants.VIDEO_ID_MEDINA,
//                    false
//                )
//            }
//        }
    }

    override fun onResume()
    {
        super.onResume()
        installInAppUpdate()
        billing.checkSubPurchase()
    }

    fun installInAppUpdate()
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
                        AppUpdateType.IMMEDIATE,
                        this,
                        100
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed()
    {
        if (Constants.INTERSTITIAL_AD_SHOWN)
        {
            return
        }
        if (supportFragmentManager.backStackEntryCount == 0)
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
            } else
            {
                showCustomDialog()
            }
        } else
        {
            super.onBackPressed()
        }
    }

    private fun getQuran(): Quran
    {
        getQuranSettings()

        val quranJson = StringBuilder()
        val inputStream = assets?.open(
            Constants.CURRENT_SCRIPT + "_" + Constants.CURRENT_TRANSLATION + ".json"
//            Constants.CURRENT_SCRIPT +".json"

        )!!
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null)
        {
            quranJson.append(line)
            quranJson.append('\n')
        }
        inputStream.close()
        bufferedReader.close()
        return Gson().fromJson(quranJson.toString(), Quran::class.java)
    }

    private fun getQuranSettings()
    {
        Constants.CURRENT_SCRIPT = sharedPreferences.getString(
            Constants.KEY_SCRIPT,
            Constants.SCRIPTS.keys.toList()[0]
        )!!
        Constants.CURRENT_SCRIPT_FONT = sharedPreferences.getString(
            Constants.KEY_SCRIPT_FONT,
            Constants.AR_FONTS.values.toList()[0]
        )!!
        Constants.CURRENT_TRANSLATION = sharedPreferences.getString(
            Constants.KEY_TRANSLATION,
            Constants.TRANSLATIONS.keys.toList()[0]
        )!!
        Constants.CURRENT_TRANSLATION_FONT = sharedPreferences.getString(
            Constants.KEY_TRANSLATION_FONT,
            Constants.EN_FONTS.values.toList()[0]
        )!!
        Constants.CURRENT_ZOOM = sharedPreferences.getInt(
            Constants.KEY_ZOOM,
            100
        )
    }

    fun loadAd()
    {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, Constants.INTERSTITIAL_AD_ID, adRequest, object : InterstitialAdLoadCallback()
        {
            override fun onAdFailedToLoad(adError: LoadAdError)
            {
                interstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd)
            {
                this@MainActivity.interstitialAd = interstitialAd

                this@MainActivity.interstitialAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback()
                    {
                        override fun onAdDismissedFullScreenContent()
                        {
                            // Called when ad is dismissed.
                            Constants.INTERSTITIAL_AD_SHOWN = false
                        }

                        override fun onAdShowedFullScreenContent()
                        {
                            // Called when ad is shown.
                            Constants.INTERSTITIAL_AD_SHOWN = true
                        }
                    }

                this@MainActivity.interstitialAd?.show(this@MainActivity)
            }
        })
    }

    override fun onClick(v: View?)
    {
        val isSubscribed = sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false)
        lastView = v!!
        when (v.id)
        {
            R.id.quran ->
            {
                toolbar.title = resources.getString(R.string.app_name)
                showFragment(QuranNavFragment.newInstance())
                resetSideMenuColor()
            }
            R.id.prayer ->
            {
                if (checkInternetConnection())
                {
                    if (checkPermission())
                    {
                        toolbar.title = resources.getString(R.string.prayer_times)
                        if (!isSubscribed)
                            loadAd()
                        showFragment(NamazFragment.newInstance())
                        resetSideMenuColor()
                    } else
                    {
                        requestPermission()
                    }
                }
            }
            R.id.settings ->
            {
                toolbar.title = resources.getString(R.string.settings)
                if (sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false))
                    if (!isSubscribed)
                        loadAd()
                showFragment(SettingsFragment.newInstance())
                resetSideMenuColor()
            }
            R.id.mosque ->
            {
                if (checkInternetConnection())
                {
                    if (checkPermission())
                    {
                        toolbar.title = resources.getString(R.string.mosque)
                        showFragment(MapsFragment.newInstance())
                        resetSideMenuColor()
                    } else
                    {
                        requestPermission()
                    }
                }
            }
            R.id.zakat ->
            {
                if (checkInternetConnection())
                {
                    toolbar.title = resources.getString(R.string.zakat)
                    if (!isSubscribed)
                        loadAd()
                    showFragment(ZakatFragment.newInstance())
                    resetSideMenuColor()
                }
            }
            R.id.qibla ->
            {
                if (checkInternetConnection())
                {
                    if (checkPermission())
                    {
                        toolbar.title = resources.getString(R.string.qibla)
                        if (!isSubscribed)
                            loadAd()
                        showFragment(QiblaFragment.newInstance())
                        resetSideMenuColor()
                    } else
                    {
                        requestPermission()
                    }
                }
            }
            R.id.mecca ->
            {
                if (checkInternetConnection())
                {
                    toolbar.title = resources.getString(R.string.live_mecca)
                    showFragment(YoutubeFragment.newInstance(Constants.VIDEO_ID_MECCA))
                    resetSideMenuColor()
                }
            }
            R.id.medina ->
            {
                if (checkInternetConnection())
                {
                    toolbar.title = resources.getString(R.string.live_medina)
                    showFragment(YoutubeFragment.newInstance(Constants.VIDEO_ID_MEDINA))
                    resetSideMenuColor()
                }
            }
            R.id.asmaAlHusna ->
            {
                if (checkInternetConnection())
                {
                    toolbar.title = resources.getString(R.string.asmaalhusna)
                    if (!isSubscribed)
                        loadAd()
                    showFragment(AsmaAlHusnaFragment.newInstance())
                    resetSideMenuColor()
                }
            }
            R.id.share ->
            {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_data))
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
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data =
                        Uri.parse(getString(R.string.store_url))
                    startActivity(openURL)
                }
            }
            R.id.removeAds ->
            {
                if (checkInternetConnection())
                {
                    try
                    {
                        billing.launchPurchaseFlow()
                    } catch (e: Exception)
                    {
                        Log.e("billing", "onClick: " + e.message)
                    }
                }
            }
        }
        slidingRootNav.closeMenu()
    }

    private fun resetSideMenuColor()
    {
        quran.setTextColor(getColor(R.color.grey))
        prayer.setTextColor(getColor(R.color.grey))
        setting.setTextColor(getColor(R.color.grey))
        mosque.setTextColor(getColor(R.color.grey))
        zakat.setTextColor(getColor(R.color.grey))
        qibla.setTextColor(getColor(R.color.grey))
        mecca.setTextColor(getColor(R.color.grey))
        medina.setTextColor(getColor(R.color.grey))
        asmaAlHusna.setTextColor(getColor(R.color.grey))
        (lastView as TextView).setTextColor(getColor(R.color.purple))
    }

    fun showCustomDialog(layout: Int = -1)
    {
        if (progressDialog == null)
        {
            when (layout)
            {
                -1 ->
                {
                    progressDialog = AlertDialog.Builder(this).apply {
                        setTitle(getString(R.string.exit))
                        setMessage(getString(R.string.msg_exit))
                        setPositiveButton(getString(R.string.yes)) { _, _ ->
                            exitProcess(0)
                        }
                        setNegativeButton(getString(R.string.no)) { _, _ ->
                            hideDialog()
                        }
                    }.create()
                }
                else ->
                {
                    progressDialog = AlertDialog.Builder(this).setView(layout).create()
                }
            }
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        }
    }

    fun hideDialog()
    {
        if (progressDialog != null && progressDialog!!.isShowing)
        {
            progressDialog!!.dismiss()
            progressDialog = null
        }
    }

    fun checkInternetConnection(): Boolean
    {
        val connectionManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConnection = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileDataConnection = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiConnection!!.isConnectedOrConnecting || mobileDataConnection!!.isConnectedOrConnecting)
        {
            return true
        }
        showSnackBar(getString(R.string.msg_connect_internet), true)
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

    private fun requestPermission()
    {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            0
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        if (requestCode == 0 && grantResults.isNotEmpty())
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                onClick(lastView)
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

    private fun showFragment(fragment: Fragment)
    {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commitAllowingStateLoss()
    }

    private fun showYoutubeFragment(
        fragment: YouTubePlayerSupportFragmentX,
        vId: String,
        newTransaction: Boolean
    )
    {
        fragment.initialize(
            Constants.API_KEY,
            object : YouTubePlayer.OnInitializedListener
            {
                @SuppressLint("SourceLockedOrientationActivity")
                override fun onInitializationSuccess(
                    provider: YouTubePlayer.Provider?,
                    player: YouTubePlayer,
                    wasRestored: Boolean
                )
                {
                    player.addFullscreenControlFlag(FULLSCREEN_FLAG_CONTROL_ORIENTATION)
                    if (newTransaction)
                    {
                        player.setFullscreen(false)
                    }
                    if (!wasRestored)
                    {
                        player.loadVideo(vId)
                    }
                    player.play()

                    player.setOnFullscreenListener { isFullSize ->
                        requestedOrientation = if (isFullSize)
                        {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        } else
                        {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        }
                    }
                }

                override fun onInitializationFailure(
                    arg0: YouTubePlayer.Provider?,
                    arg1: YouTubeInitializationResult?
                )
                {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.msg_try_later),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        if (newTransaction)
        {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment, vId)
            transaction.commitAllowingStateLoss()
        }
    }
}