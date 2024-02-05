package com.iqra.alquran.views

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel

class ZakatFragment : Fragment() {

    var currency = ""
    var nisab = ""
    var nisabValue = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = activity as MainActivity
        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_zakat, container, false)
        val spCurrency = view.findViewById<Spinner>(R.id.spCurrency)
        val spNisab = view.findViewById<Spinner>(R.id.spNisab)
        val etNisab = view.findViewById<EditText>(R.id.etNisab)
        val etAsset = view.findViewById<EditText>(R.id.etAsset)
        val tvZakat = view.findViewById<TextView>(R.id.tvZakat)
        val tvFaqs = view.findViewById<TextView>(R.id.tvFaqs)

        tvFaqs.paintFlags = tvFaqs.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val currencyAdapter = ArrayAdapter(
            mainActivity,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.currencies_keys)
        )
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCurrency.adapter = currencyAdapter

        spCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(mainActivity.checkInternetConnection()) {
                    currency = resources.getStringArray(R.array.currencies_values)[position]
                    viewModel.getForexRates(currency)

                    etAsset.setText("")
                    tvZakat.setText("$currency 0")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val nisabAdapter = ArrayAdapter(
            mainActivity,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.nisab_keys)
        )
        nisabAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNisab.adapter = nisabAdapter

        spNisab.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(mainActivity.checkInternetConnection()) {
                    nisab = resources.getStringArray(R.array.nisab_keys)[position]
                    viewModel.getForexRates(currency)

                    etAsset.setText("")
                    tvZakat.setText("$currency 0")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        viewModel.forexRates.observe(viewLifecycleOwner) {
            if(it.data != null) {
                mainActivity.hideDialog()

                if (it.data.items.isNotEmpty()) {
                    Log.e("nisab", it.data.items[0].toString())

                    if (nisab == resources.getStringArray(R.array.nisab_keys)[0]) {
                        val xauValue = it.data.items[0].xauPrice.toDouble()
                        nisabValue = (xauValue * 2.8125473).toInt()

                    } else {
                        val xagValue = it.data.items[0].xagPrice.toDouble()
                        nisabValue = (xagValue * 19.6878312).toInt()
                    }
                    etNisab.setText(nisabValue.toString())
                }
            }else if(it.message != null){
                mainActivity.hideDialog()

                val message = when (it.message) {
                    Constants.MSG_TRY_LATER -> {
                        getString(R.string.msg_try_later)
                    }
                    Constants.MSG_CONNECT_INTERNET -> {
                        getString(R.string.msg_connect_internet)
                    }
                    else -> {
                        it.message
                    }
                }

                mainActivity.showSnackBar(message, message == Constants.MSG_CONNECT_INTERNET)
            }else{
                mainActivity.showCustomDialog(R.layout.progress_dialog)
            }
        }

        etAsset.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!etAsset.text.isNullOrEmpty()) {
                    val assetVal = etAsset.text.toString().toLongOrNull()
                    if (assetVal == null) {
                        return
                    } else if (assetVal >= nisabValue) {
                        tvZakat.text = "$currency " + (assetVal * .025).toLong()
                    } else {
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

    companion object {
        @JvmStatic
        fun newInstance() = ZakatFragment()
    }
}