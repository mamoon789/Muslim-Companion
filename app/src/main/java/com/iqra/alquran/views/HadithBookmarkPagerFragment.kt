package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Chapter
import com.iqra.alquran.network.models.Hadith
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HadithBookmarkPagerFragment : Fragment()
{
    private lateinit var mainActivity: MainActivity
    private lateinit var rvBookmarks: RecyclerView
    private lateinit var bookmarks: List<Hadith.Hadiths.Data>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        mainActivity = activity as MainActivity
        mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE).apply {
            bookmarks = getStringSet(Constants.KEY_HADITH_BOOKMARKS, emptySet())!!
                .map {
                    Gson().fromJson(it, Hadith.Hadiths.Data::class.java)
                }
        }

        val view = inflater.inflate(R.layout.fragment_hadith_bookmark_pager, container, false)
        rvBookmarks = view.findViewById(R.id.rvBookmarks)
        rvBookmarks.adapter = Adapter()
        rvBookmarks.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        return view
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_hadith_bookmark, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.apply {
                bookmarks[position].let { bookmark ->
                    tvNo.text = bookmark.hadithNumber
                    tvHadith.text = bookmark.hadithEnglish
                    tvBookChapter.text =
                        "${bookmark.book.bookName}, ${bookmark.chapter.chapterEnglish}"

                    view.setOnClickListener {
                        mainActivity.supportFragmentManager.beginTransaction().apply {
                            replace(R.id.container, HadithFragment.newInstance(
                                    bookmark.bookSlug,
                                    bookmark.chapter.chapterNumber,
                                    bookmark.hadithEnglish
                                )
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
            return bookmarks.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
        {
            val tvNo: TextView = view.findViewById(R.id.tvNo)
            val tvHadith: TextView = view.findViewById(R.id.tvHadith)
            val tvBookChapter: TextView = view.findViewById(R.id.tvBookChapter)
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = HadithBookmarkPagerFragment()
    }
}