package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.rvadapter.AdmobNativeAdAdapter
import com.google.gson.Gson
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.R
import com.iqra.alquran.network.models.AsmaAlHusna
import com.iqra.alquran.network.models.Hadith
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel

class AsmaAlHusnaFragment : Fragment()
{

    private lateinit var mainActivity: MainActivity
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: Adapter
    private lateinit var adapterAd: AdmobNativeAdAdapter
    private lateinit var rvAsmaAlHusna: RecyclerView
    private var asmaAlHusna = listOf<AsmaAlHusna.Data>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainActivity = activity as MainActivity
        sharedPreferences = mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE)

        adapter = Adapter()
        adapterAd = AdmobNativeAdAdapter.Builder.with(
            if (BuildConfig.DEBUG) Constants.NATIVE_AD_ID else BuildConfig.NATIVE_AD_ID,
            adapter,
            "medium"
        )
            .adItemInterval(5)
            .build()

        viewModel.getAsmaAlHusna()

        viewModel.asmaAlHusna.observe(this) {
            mainActivity.hideDialog()
            if (it.data != null)
            {
                mainActivity.showPremiumDialogOrAd()
                asmaAlHusna = it.data.data
                adapter.notifyDataSetChanged()
            } else if (it.message != null)
            {
                mainActivity.showSnackBar(it.message, it.message == Constants.MSG_CONNECT_INTERNET)
            } else
            {
                mainActivity.showCustomDialog(R.layout.dialog_progress)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_asmaalhusna, container, false)
        rvAsmaAlHusna = view.findViewById(R.id.rvAsmaAlHusna)

        if (!sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false))
        {
            rvAsmaAlHusna.adapter = adapterAd
        } else
        {
            rvAsmaAlHusna.adapter = adapter
        }

        return view
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>()
    {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
        {
            val tvNumber: TextView = view.findViewById(R.id.tvNumber)
            val tvNameEnglish: TextView = view.findViewById(R.id.tvNameEnglish)
            val tvNameArabic: TextView = view.findViewById(R.id.tvNameArabic)
            val tvMeaning: TextView = view.findViewById(R.id.tvMeaning)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_asmaalhusna, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.tvNumber.text = "${asmaAlHusna[position].number}. "
            holder.tvNameEnglish.text = asmaAlHusna[position].transliteration
            holder.tvNameArabic.text = asmaAlHusna[position].name
            holder.tvMeaning.text = asmaAlHusna[position].en.meaning
        }

        override fun getItemCount(): Int
        {
            return asmaAlHusna.size
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = AsmaAlHusnaFragment()
    }
}