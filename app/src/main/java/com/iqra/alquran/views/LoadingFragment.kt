package com.iqra.alquran.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import com.iqra.alquran.R
import com.iqra.alquran.application.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingFragment : Fragment()
{
    private lateinit var myApplication: MyApplication
    private lateinit var mainActivity: MainActivity
    lateinit var observer: Observer<Int>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.hide()
        myApplication = mainActivity.application as MyApplication

        val view = inflater.inflate(R.layout.fragment_loading, container, false)
        observer = Observer {
            when (it)
            {
                0 -> myApplication.appOpenAdManager.showAdIfAvailable(mainActivity)
                1 -> mainActivity.supportFragmentManager.popBackStack()
                else ->
                {
                }
            }
        }
        myApplication.event.observe(viewLifecycleOwner, observer)
        return view
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mainActivity.supportActionBar?.show()
        myApplication.event.postValue(-1)
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = LoadingFragment()
    }
}