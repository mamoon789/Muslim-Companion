package com.iqra.alquran.views

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.iqra.alquran.R


class YoutubeFragment : Fragment()
{
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_youtube, container, false)
        val webView: WebView = view.findViewById(R.id.webview)
        val videoId = arguments?.getString("videoId","")
        val htmlCode =
            "<html><body style='margin:0;padding:0;'><iframe allow='fullscreen;' id='player' type='text/html' width='100%' height='100%' src='https://www.youtube.com/embed/$videoId?enablejsapi=1' frameborder='0'></iframe></body></html>"
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = CustomWebChromeClient(activity as MainActivity, view)
        webView.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)

        return view
    }

    companion object
    {
        @JvmStatic
        fun newInstance(videoId: String) =
            YoutubeFragment().apply {
                arguments = Bundle().apply {
                    putString("videoId", videoId)
                }
            }
    }
}

class CustomWebChromeClient(val activity: MainActivity, view: View) : WebChromeClient()
{
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalOrientation = 0
    private val fullscreenContainer: FrameLayout = view.findViewById(R.id.fullscreenContainer)

    override fun onHideCustomView()
    {
        if (customView == null)
        {
            return
        }
        activity.supportActionBar?.show()
        activity.slidingRootNav.isMenuLocked = false
        fullscreenContainer.removeView(customView)
        fullscreenContainer.visibility = View.GONE
        customView = null
        customViewCallback!!.onCustomViewHidden()
        activity.requestedOrientation = originalOrientation
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback)
    {
        if (customView != null)
        {
            onHideCustomView()
            return
        }
        activity.supportActionBar?.hide()
        activity.slidingRootNav.isMenuLocked = true
        customView = view
        originalOrientation = activity.requestedOrientation
        customViewCallback = callback
        fullscreenContainer.addView(
            customView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        fullscreenContainer.visibility = View.VISIBLE
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}