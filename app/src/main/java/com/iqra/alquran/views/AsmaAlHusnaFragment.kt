package com.iqra.alquran.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.iqra.alquran.R
import com.iqra.alquran.network.models.AsmaAlHusna
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel

class AsmaAlHusnaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        val mainActivity = activity as MainActivity
        val view = inflater.inflate(R.layout.fragment_asmaalhusna, container, false)
        val rvAsmaAlHusna = view.findViewById<RecyclerView>(R.id.rvAsmaAlHusna)

        viewModel.getAsmaAlHusna()

        viewModel.asmaAlHusna.observe(viewLifecycleOwner) {
            if (it.data != null) {
                mainActivity.hideDialog()
                rvAsmaAlHusna.adapter = Adapter(it.data.data)

            } else if (it.message != null) {
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
            } else {
                mainActivity.showCustomDialog(R.layout.progress_dialog)
            }
        }

        return view
    }

    class Adapter(private val asmaAlHusna: MutableList<AsmaAlHusna.Data>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNumber: TextView = view.findViewById(R.id.tvNumber)
            val tvNameEnglish: TextView = view.findViewById(R.id.tvNameEnglish)
            val tvNameArabic: TextView = view.findViewById(R.id.tvNameArabic)
            val tvMeaning: TextView = view.findViewById(R.id.tvMeaning)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.asmaalhusna_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvNumber.text = asmaAlHusna[position].number.toString()
            holder.tvNameEnglish.text = asmaAlHusna[position].transliteration
            holder.tvNameArabic.text = asmaAlHusna[position].name
            holder.tvMeaning.text = asmaAlHusna[position].en.meaning
        }

        override fun getItemCount(): Int {
            return asmaAlHusna.size
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AsmaAlHusnaFragment()
    }
}