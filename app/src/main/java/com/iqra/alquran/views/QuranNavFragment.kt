package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Quran
import com.iqra.alquran.utils.Constants
import java.io.BufferedReader
import java.io.InputStreamReader
import com.iqra.alquran.network.models.Quran.Data.Surah

class QuranNavFragment : Fragment() {
    private lateinit var adapter: ViewPagerFragmentAdapter
    private lateinit var surahs: MutableList<Surah>
    private lateinit var language: String

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cv: CardView
    private lateinit var tvLastRead: TextView
    private lateinit var tvSurah: TextView
    private lateinit var tvAyah: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quran_nav, container, false)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        cv = view.findViewById(R.id.cardView)
        tvLastRead = view.findViewById(R.id.textView6)
        tvSurah = view.findViewById(R.id.textView7)
        tvAyah = view.findViewById(R.id.textView8)

//        tvLastRead.text = Constants.RESOURCES?.getString(R.string.last_read)

        sharedPreferences = activity!!.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )

        surahs = (activity as MainActivity).surahs
        language = (activity as MainActivity).language

        adapter = ViewPagerFragmentAdapter(
            childFragmentManager,
            lifecycle,
            mutableListOf(
                QuranSurahPagerFragment.newInstance(),
                QuranParaPagerFragment.newInstance(),
                QuranBookmarkPagerFragment.newInstance()
            )
        )
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Surah" else if (position == 1) "Juz" else "Bookmark"
        }.attach()

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        val id: String = sharedPreferences.getString(
            Constants.KEY_LAST_READ,
            "surah0_ayah0"
        )!!

        val surahIndex = id.substring(5, id.indexOf('_')).toInt()
        val ayahIndex = id.substring(id.indexOf('_') + 5).toInt()

        tvSurah.text = surahs[surahIndex].englishName;
        tvAyah.text = "Ayah No. ${ayahIndex + 1}";

        adapter.refreshFragment(2, QuranBookmarkPagerFragment.newInstance())

        cv.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(
                R.id.container,
                QuranFragment.newInstance(surahIndex, ayahIndex)
            );
            transaction?.addToBackStack(null);
            transaction?.commit();
        }
    }

    private class ViewPagerFragmentAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        var fragments: MutableList<Fragment>,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

        override fun getItemCount(): Int {
            return 3
        }

        fun refreshFragment(index: Int, fragment: Fragment) {
            fragments[index] = fragment
            notifyItemChanged(index)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = QuranNavFragment()
    }
}