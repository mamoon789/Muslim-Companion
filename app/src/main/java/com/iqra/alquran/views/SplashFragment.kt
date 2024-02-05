package com.iqra.alquran.views

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iqra.alquran.R

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        mainActivity.supportActionBar?.hide()
        mainActivity.installInAppUpdate()

        Handler().postDelayed({
            mainActivity.onClick(mainActivity.quran)
            mainActivity.supportActionBar?.show()
        }, 3000)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = SplashFragment()
    }
}