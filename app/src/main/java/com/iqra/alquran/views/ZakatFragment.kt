package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.iqra.alquran.R
import com.iqra.alquran.application.MyApplication
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.utils.Utility
import com.iqra.alquran.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ZakatFragment : Fragment()
{
    lateinit var mainActivity: MainActivity
    lateinit var viewModel: MainViewModel
    lateinit var adContainer: FrameLayout
    lateinit var currency: String
    lateinit var nisab: String
    var currencyPosition = 0
    var nisabPosition = 0
    var nisabValue = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        nisab = resources.getStringArray(R.array.nisab_keys)[nisabPosition]
        currency = resources.getStringArray(R.array.currencies_values)[currencyPosition]
        viewModel.getForexRates(currency)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_zakat, container, false)
        val spCurrency = view.findViewById<Spinner>(R.id.spCurrency)
        val spNisab = view.findViewById<Spinner>(R.id.spNisab)
        val etNisab = view.findViewById<EditText>(R.id.etNisab)
        val etAsset = view.findViewById<EditText>(R.id.etAsset)
        val tvZakat = view.findViewById<TextView>(R.id.tvZakat)
        val tvFaqs = view.findViewById<TextView>(R.id.tvFaqs)
        adContainer = view.findViewById(R.id.adContainer)

        mainActivity.loadBanner(adContainer)

        tvFaqs.paintFlags = tvFaqs.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val currencyAdapter = ArrayAdapter(
            mainActivity,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.currencies_keys)
        )
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCurrency.adapter = currencyAdapter
        spCurrency.setSelection(currencyPosition, false)

        spCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            )
            {
                currencyPosition = position
                if (mainActivity.checkInternetConnection())
                {
                    currency = resources.getStringArray(R.array.currencies_values)[currencyPosition]
                    viewModel.getForexRates(currency)

                    etAsset.setText("")
                    tvZakat.text = "$currency 0"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
        }

        val nisabAdapter = ArrayAdapter(
            mainActivity,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.nisab_keys)
        )
        nisabAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNisab.adapter = nisabAdapter
        spNisab.setSelection(nisabPosition, false)

        spNisab.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            )
            {
                nisabPosition = position
                if (mainActivity.checkInternetConnection())
                {
                    nisab = resources.getStringArray(R.array.nisab_keys)[nisabPosition]
                    viewModel.getForexRates(currency)

                    etAsset.setText("")
                    tvZakat.text = "$currency 0"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
        }

        viewModel.forexRates.observe(viewLifecycleOwner) {
            mainActivity.hideDialog()
            if (it.data != null)
            {
                if (it.data.items.isNotEmpty())
                {
                    nisabValue = if (nisab == resources.getStringArray(R.array.nisab_keys)[0])
                    {
                        val xauValue = it.data.items[0].xauPrice.toDouble()
                        (xauValue / 31.1035 * 87.48).toInt()

                    } else
                    {
                        val xagValue = it.data.items[0].xagPrice.toDouble()
                        (xagValue / 28.35 * 612.36).toInt()
                    }
                    etNisab.setText(nisabValue.toString())
                }
            } else if (it.message != null)
            {
                mainActivity.showSnackBar(it.message, it.message == Constants.MSG_CONNECT_INTERNET)
            } else
            {
                mainActivity.showCustomDialog(R.layout.dialog_progress)
            }
        }

        etAsset.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (!etAsset.text.isNullOrEmpty())
                {
                    val assetVal = etAsset.text.toString().toLongOrNull()
                    if (assetVal == null)
                    {
                        return
                    } else if (assetVal >= nisabValue)
                    {
                        tvZakat.text = "$currency " + (assetVal * .025).toLong()
                    } else
                    {
                        tvZakat.text = "$currency 0"
                    }
                }
            }
        })

        tvFaqs.setOnClickListener {
            val transaction = mainActivity.supportFragmentManager.beginTransaction()
            transaction.replace(
                R.id.container,
                FaqsFragment.newInstance()
            );
            transaction.addToBackStack(null);
            transaction.commit();
        }
        return view
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = ZakatFragment()
    }
}