package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants
import java.io.Serializable
import com.iqra.alquran.network.models.Quran.Data.Surah

class QuranBookmarkPagerFragment : Fragment() {
    private lateinit var surahs: MutableList<Surah>
    lateinit var edition: String
    private var bookmarksList: MutableList<String>? = null
    private var adapter: QuranBookmarkPagerFragment.Adapter? = null
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        surahs = (activity as MainActivity).surahs
        edition = (activity as MainActivity).language

        sharedPreferences = activity?.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        bookmarksList = sharedPreferences?.getStringSet(Constants.KEY_BOOKMARKS, setOf<String>())
            ?.toMutableList()

        val view = inflater.inflate(R.layout.fragment_quran_bookmark_pager, container, false)
        val rvBookmarks = view.findViewById<RecyclerView>(R.id.rvBookmarks)
        adapter = Adapter()
        rvBookmarks.adapter = adapter
        rvBookmarks.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        return view
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.bookmark_row, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bookmarkId = bookmarksList!!.get(position)

            holder.surahIndex = bookmarkId.substring(5, bookmarkId.indexOf('_')).toInt()
            holder.ayahIndex = bookmarkId.substring(bookmarkId.indexOf('_') + 5).toInt()

            val surah = surahs.get(holder.surahIndex)
            holder.tvNo.text = (position + 1).toString()
            holder.tvSurah.text = surah.englishName
            holder.tvVerse.text = "Verse " + holder.ayahIndex + 1
            holder.tvSurahArabic.text = surah.name
        }

        override fun getItemCount(): Int {
            return bookmarksList!!.size
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
        fun newInstance() = QuranBookmarkPagerFragment()
    }
}