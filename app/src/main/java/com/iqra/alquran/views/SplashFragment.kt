package com.iqra.alquran.views

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.iqra.alquran.R
import com.iqra.alquran.application.MyApplication
import com.iqra.alquran.utils.Constants
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.effet.RippleEffect
import com.takusemba.spotlight.shape.Circle

class SplashFragment : Fragment()
{
    lateinit var sharedPreferences: SharedPreferences
    lateinit var myApplication: MyApplication
    lateinit var mainActivity: MainActivity
    lateinit var gif: ImageView
    lateinit var logo: ImageView
    lateinit var observer: Observer<Int>
    var showShowcase = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        mainActivity = activity as MainActivity
        myApplication = mainActivity.application as MyApplication
        sharedPreferences =
            mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE).apply {
                showShowcase = getBoolean(Constants.KEY_SHOWCASE, true)
            }

        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        gif = view.findViewById(R.id.gif)
        logo = view.findViewById(R.id.logo)

        observer = Observer {
            when (it)
            {
                -1 -> mainActivity.supportActionBar?.hide()
                0 -> myApplication.appOpenAdManager.showAdIfAvailable(mainActivity)
                1 ->
                {
                    logo.visibility = View.GONE
                    loadGif()
                }
            }
        }
        myApplication.event.observe(viewLifecycleOwner, observer)

        return view
    }

    override fun onDestroy()
    {
        super.onDestroy()
        myApplication.event.removeObserver(observer)
        myApplication.event.postValue(-1)
    }

    private fun loadGif()
    {
        Glide.with(this@SplashFragment)
            .asGif()
            .listener(object : RequestListener<GifDrawable>
            {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean
                {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean
                {
                    resource.setLoopCount(1)
                    resource.registerAnimationCallback(object :
                        Animatable2Compat.AnimationCallback()
                    {
                        @Suppress("NAME_SHADOWING")
                        override fun onAnimationEnd(drawable: Drawable?)
                        {
                            super.onAnimationEnd(drawable)
                            Log.d("Glide", "GIF animation completed")
                            mainActivity.onClick(mainActivity.quran)
                            mainActivity.supportActionBar?.show()

                            if (showShowcase)
                            {
                                sharedPreferences.edit().putBoolean(Constants.KEY_SHOWCASE, false)
                                    .apply()

                                val hamburgerView = getHamburgerIcon()!!
                                hamburgerView.doOnPreDraw {
                                    val root = FrameLayout(requireContext())
                                    val view =
                                        layoutInflater.inflate(R.layout.showcase_layout, root)
                                    val ok = view.findViewById<Button>(R.id.button)

                                    val target = com.takusemba.spotlight.Target.Builder()
                                        .setAnchor(hamburgerView)
                                        .setShape(Circle(75f))
                                        .setEffect(
                                            RippleEffect(
                                                75f,
                                                175f,
                                                resources.getColor(R.color.purple, null)
                                            )
                                        )
                                        .setOverlay(view)
                                        .build()

                                    val spotlight = Spotlight.Builder(mainActivity)
                                        .setTargets(target)
                                        .setBackgroundColor(
                                            resources.getColor(
                                                R.color.showcase_bg, null
                                            )
                                        )
                                        .setDuration(1000L)
                                        .setAnimation(DecelerateInterpolator(2f))
                                        .build()

                                    spotlight.start()

                                    view.setOnClickListener(null)
                                    ok.setOnClickListener {
                                        spotlight.finish()
                                    }
                                }
                            }
                        }
                    })
                    return false
                }
            })
            .load(R.drawable.bismillah_calligraphy).into(gif)
    }

    private fun getHamburgerIcon(): View?
    {
        val toolbar = mainActivity.toolbar
        for (i in 0 until toolbar.childCount)
        {
            val child = toolbar.getChildAt(i)
            if (child is ImageButton)
            {
                return child
            }
        }
        return null
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = SplashFragment()
    }
}