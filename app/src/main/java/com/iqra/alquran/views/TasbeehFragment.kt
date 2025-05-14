package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Dhikr
import com.iqra.alquran.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TasbeehFragment : Fragment()
{
    private lateinit var mainActivity: MainActivity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dhikr: Dhikr
    private lateinit var tvDhikr: TextView
    private lateinit var tvDetail: TextView
    private lateinit var tvProgress: TextView
    private lateinit var pbProgress: ProgressBar
    private var position = -1
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        sharedPreferences = mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        arguments?.let {
            dhikr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                it.getSerializable("dhikr", Dhikr::class.java)!!
            } else
            {
                it.getSerializable("dhikr") as Dhikr
            }
            position = it.getInt("position")
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(250)
            mainActivity.showPremiumDialogOrAd()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_tasbeeh, container, false)
        view.let {
            tvDhikr = it.findViewById(R.id.tvDhikr)
            tvDetail = it.findViewById(R.id.tvDetail)
            tvProgress = it.findViewById(R.id.tvProgress)
            pbProgress = it.findViewById(R.id.pbProgress)
        }

        dhikr.data[position].let { dkr ->
            tvDhikr.text = dkr.content
            tvDetail.text = dkr.description
            updateProgressView(dkr.progress, dkr.count)
        }

        view.setOnClickListener {
            dhikr.data[position].apply {
                progress = progress.toInt().inc().toString()
                updateProgressView(progress, count)
            }
        }

        return view
    }

    private fun updateProgressView(progress: String, count: String)
    {
        tvProgress.text = progress
        pbProgress.setProgress(100 * progress.toInt() / count.toInt(), true)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume()
    {
        super.onResume()
        mainActivity.toolbar.inflateMenu(R.menu.menu2)
        mainActivity.toolbar.setOnMenuItemClickListener {
            mainActivity.showCustomDialog(
                title = "Reset Dhikr",
                message = "Are you sure you want to reset?",
                positiveListener = { _, _ ->
                    dhikr.data[position].apply {
                        progress = "0"
                        updateProgressView(progress, count)
                    }
                    mainActivity.hideDialog()
                }
            )
            false
        }
    }

    override fun onPause()
    {
        super.onPause()
        sharedPreferences.edit().putString(Constants.KEY_TASBEEH_DHIKRS, Gson().toJson(dhikr))
            .apply()
        mainActivity.toolbar.menu.clear()
    }

    companion object
    {
        @JvmStatic
        fun newInstance(dhikr: Dhikr, position: Int) =
            TasbeehFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("dhikr", dhikr)
                    putInt("position", position)
                }
            }
    }
}