package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Book
import com.iqra.alquran.network.models.Chapter
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class HadithNavFragment : Fragment()
{
    private lateinit var mainActivity: MainActivity
    private lateinit var cvSearch: CardView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: ViewPagerFragmentAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        mainActivity = activity as MainActivity
        val view = inflater.inflate(R.layout.fragment_hadith_nav, container, false)
        cvSearch = view.findViewById(R.id.cvSearch)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        adapter = ViewPagerFragmentAdapter(
            childFragmentManager,
            lifecycle,
            mutableListOf(
                HadithBookPagerFragment.newInstance(),
                HadithBookmarkPagerFragment.newInstance()
            )
        )
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Book" else "Bookmark"
        }.attach()

        cvSearch.setOnClickListener {
            mainActivity.supportFragmentManager.beginTransaction().apply {
                replace(
                    R.id.container,
                    HadithSearchFragment.newInstance()
                );
                addToBackStack(null);
                commit();
            }
        }

        return view
    }

    private class ViewPagerFragmentAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        var fragments: MutableList<Fragment>,
    ) : FragmentStateAdapter(fragmentManager, lifecycle)
    {

        override fun createFragment(position: Int): Fragment
        {
            return fragments[position]
        }

        override fun getItemCount(): Int
        {
            return 2
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = HadithNavFragment()
    }
}