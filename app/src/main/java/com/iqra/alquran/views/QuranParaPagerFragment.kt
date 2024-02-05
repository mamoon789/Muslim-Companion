package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iqra.alquran.R
import java.io.Serializable
import com.iqra.alquran.network.models.Quran.Data.Surah
import com.iqra.alquran.network.models.Quran.Data.Surah.Ayah

@Suppress("UNCHECKED_CAST")
class QuranParaPagerFragment : Fragment() {
    lateinit var surahs: MutableList<Surah>
    lateinit var surahsTemp: MutableList<Surah>
    lateinit var ayahs: MutableList<Ayah>
    lateinit var edition: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        surahs = (activity as MainActivity).surahs
        edition = (activity as MainActivity).language
        ayahs = surahs.flatMap { it.ayahs }.distinctBy { it.juz } as MutableList<Ayah>
        surahsTemp = mutableListOf()
        for (ayah in ayahs) {
            for (surah in surahs) {
                if (surah.ayahs.contains(ayah)) {
                    surahsTemp.add(surah)
                    break
                }
            }
        }
        val view = inflater.inflate(R.layout.fragment_quran_para_pager, container, false)
        val rvParas = view.findViewById<RecyclerView>(R.id.rvParas)
        rvParas.adapter = Adapter()
        rvParas.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        return view
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.para_row, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.surahIndex = surahsTemp.get(position).number - 1
            holder.ayahIndex = ayahs.get(position).numberInSurah - 1
            holder.tvPara.text = "Juz " + ayahs.get(position).juz.toString()
        }

        override fun getItemCount(): Int {
            return ayahs.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            var surahIndex: Int = 0
            var ayahIndex: Int = 0
            val tvPara: TextView = view.findViewById(R.id.tvPara)

            init {
                view.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                transaction?.replace(
                    R.id.container,
                    QuranFragment.newInstance(surahIndex, ayahIndex)
                );
                transaction?.addToBackStack(null);
                transaction?.commit();
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = QuranParaPagerFragment()
    }
}