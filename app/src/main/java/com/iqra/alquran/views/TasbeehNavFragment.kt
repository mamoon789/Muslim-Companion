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
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.google.android.ads.nativetemplates.rvadapter.AdmobNativeAdAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Dhikr
import com.iqra.alquran.utils.Constants

class TasbeehNavFragment : Fragment()
{
    private lateinit var mainActivity: MainActivity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rvDhikr: RecyclerView
    private lateinit var dhikr: Dhikr
    private lateinit var adapter: Adapter
//    private lateinit var adapterAd: AdmobNativeAdAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        sharedPreferences =
            mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE).apply {
                dhikr = Gson().fromJson(
                    getString(Constants.KEY_TASBEEH_DHIKRS, defaultDhikrs()),
                    Dhikr::class.java
                )
            }

        adapter = Adapter()
//        adapterAd = AdmobNativeAdAdapter.Builder.with(
//            if (BuildConfig.DEBUG) Constants.NATIVE_AD_ID else BuildConfig.NATIVE_AD_ID,
//            adapter,
//            "medium"
//        )
//            .adItemInterval(5)
//            .build()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_tasbeeh_nav, container, false)
        rvDhikr = view.findViewById(R.id.rvDhikr)
        if (!sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false))
        {
            rvDhikr.adapter = adapter
        } else
        {
            rvDhikr.adapter = adapter
        }
        rvDhikr.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume()
    {
        super.onResume()
        mainActivity.toolbar.inflateMenu(R.menu.menu)
        mainActivity.toolbar.setOnMenuItemClickListener {
            val inflater = LayoutInflater.from(mainActivity)
            val view = inflater.inflate(R.layout.dialog_dhikr, null, false).apply {
                val etDhikr: EditText = findViewById(R.id.etDhikr)
                val etDetail: EditText = findViewById(R.id.etDetail)
                val etRepeat: EditText = findViewById(R.id.etRepeat)
                val btSave: Button = findViewById(R.id.btSave)
                val ibClose: ImageButton = findViewById(R.id.ibClose)

                btSave.setOnClickListener {
                    dhikr.data.add(
                        Dhikr.Data(
                            etDhikr.text.toString(),
                            etDetail.text.toString(),
                            etRepeat.text.toString(),
                        )
                    )
                    adapter.notifyDataSetChanged()
                    sharedPreferences.edit()
                        .putString(Constants.KEY_TASBEEH_DHIKRS, Gson().toJson(dhikr))
                        .apply()
                    mainActivity.hideDialog()
                }
                ibClose.setOnClickListener {
                    mainActivity.hideDialog()
                }
            }
            mainActivity.showCustomDialog(layout = view)
            false
        }
    }

    override fun onPause()
    {
        super.onPause()
        mainActivity.toolbar.menu.clear()
    }

    private fun defaultDhikrs(): String
    {
        val inputStream = mainActivity.assets.open("dhikr.json")
        return inputStream.bufferedReader().use { it.readText() }
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_tasbeeh_dhikr, parent, false)
            return ViewHolder(view)
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.apply {
                dhikr.data[position].let { dkr ->
                    tvNo.text = "${position + 1}"
                    tvContent.text = dkr.content
                    tvProgress.text = "${dkr.progress}/${dkr.count}"
                    pbProgress.setProgress(100 * dkr.progress.toInt() / dkr.count.toInt(), true)

                    ibEdit.setOnClickListener {
                        val inflater = LayoutInflater.from(mainActivity)
                        val view = inflater.inflate(R.layout.dialog_dhikr, null, false).apply {
                            val tvHeading: TextView = findViewById(R.id.tvHeading)
                            val etDhikr: EditText = findViewById(R.id.etDhikr)
                            val etDetail: EditText = findViewById(R.id.etDetail)
                            val etRepeat: EditText = findViewById(R.id.etRepeat)
                            val btSave: Button = findViewById(R.id.btSave)
                            val ibClose: ImageButton = findViewById(R.id.ibClose)

                            tvHeading.text = "Edit Dhikr"
                            etDhikr.setText(dkr.content)
                            etDetail.setText(dkr.description)
                            etRepeat.setText(dkr.count)

                            btSave.setOnClickListener {
                                dhikr.data[position] = dkr.copy(
                                    content = etDhikr.text.toString(),
                                    description = etDetail.text.toString(),
                                    count = etRepeat.text.toString()
                                )
                                adapter.notifyDataSetChanged()
                                sharedPreferences.edit()
                                    .putString(Constants.KEY_TASBEEH_DHIKRS, Gson().toJson(dhikr))
                                    .apply()
                                mainActivity.hideDialog()
                            }
                            ibClose.setOnClickListener {
                                mainActivity.hideDialog()
                            }
                        }
                        mainActivity.showCustomDialog(layout = view)
                    }
                    ibDelete.setOnClickListener {
                        mainActivity.showCustomDialog(
                            title = "Delete Dhikr",
                            message = "Are you sure you want to delete?",
                            positiveListener = { _, _ ->
                                dhikr.data.remove(dkr)
                                adapter.notifyDataSetChanged()
                                sharedPreferences.edit()
                                    .putString(Constants.KEY_TASBEEH_DHIKRS, Gson().toJson(dhikr))
                                    .apply()
                                mainActivity.hideDialog()
                            }
                        )
                    }
                    view.setOnClickListener {
                        mainActivity.supportFragmentManager.beginTransaction().apply {
                            replace(
                                R.id.container,
                                TasbeehFragment.newInstance(dhikr, position)
                            );
                            addToBackStack(null);
                            commit();
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int
        {
            return dhikr.data.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
        {
            val tvNo: TextView = view.findViewById(R.id.tvNo)
            val tvContent: TextView = view.findViewById(R.id.tvContent)
            val tvProgress: TextView = view.findViewById(R.id.tvProgress)
            val pbProgress: ProgressBar = view.findViewById(R.id.pbProgress)
            val ibEdit: ImageButton = view.findViewById(R.id.ibEdit)
            val ibDelete: ImageButton = view.findViewById(R.id.ibDelete)
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = TasbeehNavFragment()
    }
}