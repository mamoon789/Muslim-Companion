package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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

@Suppress("UNCHECKED_CAST")
class QuranSurahPagerFragment : Fragment() {
    lateinit var surahs: MutableList<Surah>
    lateinit var edition: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quran_surah_pager, container, false)
        val rvSurahs = view.findViewById<RecyclerView>(R.id.rvSurahs)

        surahs = (activity as MainActivity).surahs
        edition = (activity as MainActivity).language

        rvSurahs.adapter = Adapter()
        rvSurahs.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        return view
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.surah_row, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.surahIndex = position
            holder.ayahIndex = 0

            val surah: Surah = surahs.get(position)
            holder.tvNo.text = surah.number.toString()
            holder.tvSurah.text = surah.englishName
            holder.tvVerse.text = surah.revelationType + " - " + surah.ayahs.size + " ayahs"
            holder.tvSurahArabic.text = surah.name
        }

        override fun getItemCount(): Int {
            return surahs.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            var surahIndex: Int = 0
            var ayahIndex: Int = 0
            val tvNo: TextView = view.findViewById(R.id.tvNo)
            val tvSurah: TextView = view.findViewById(R.id.tvSurah)
            val tvVerse: TextView = view.findViewById(R.id.tvVerse)
            val tvSurahArabic: TextView = view.findViewById(R.id.tvSurahArabic)

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
        fun newInstance() = QuranSurahPagerFragment()
    }
}