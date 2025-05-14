package com.iqra.alquran.views

import android.annotation.SuppressLint
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
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Quran.Data.Surah
import com.iqra.alquran.network.models.Quran.Data.Surah.Ayah
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class QuranParaPagerFragment : Fragment()
{
    lateinit var mainActivity: MainActivity
    lateinit var surahs: MutableList<Surah>
    lateinit var surahsByJuz: MutableList<Surah>
    lateinit var ayahsByJuz: Map<Int, List<Ayah>>
    var visibleJuzPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        mainActivity = activity as MainActivity
        surahs = mainActivity.surahs
        surahsByJuz = mutableListOf()
        ayahsByJuz = surahs.flatMap { it.ayahs }.groupBy { it.juz - 1 }

        for (ayah in ayahsByJuz)
        {
            for (surah in surahs)
            {
                if (surah.ayahs.contains(ayah.value[0]))
                {
                    surahsByJuz.add(surah)
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

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_quran_para, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.apply {
                setIsRecyclable(false)

                ayahsByJuz[position]?.let { ayahsByJuz ->

                    tvJuz.text = "${ayahsByJuz[0].juz}"
                    tvAyahArabic.text = ayahsByJuz[0].text
                    tvVerse.text =
                        "Surah ${surahsByJuz[position].englishName}, Ayah ${ayahsByJuz[0].numberInSurah}"

                    if (position == visibleJuzPosition)
                    {
                        CoroutineScope(Dispatchers.Main).launch {
                            async {
                                printHizb()
                            }.await()
                            ibHizb.setImageDrawable(
                                ContextCompat.getDrawable(
                                    view.context,
                                    R.drawable.arrow_up
                                )
                            )
                            llHizb.visibility = View.VISIBLE
                            vLine.visibility = View.GONE
                        }
                    }

                    view.tag = Bundle().apply {
                        putInt("surahIndex", surahsByJuz[position].number - 1)
                        putInt("ayahIndex", ayahsByJuz[0].numberInSurah - 1)
                    }
                }
            }
        }

        override fun getItemCount(): Int
        {
            return ayahsByJuz.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener
        {
            val tvJuz: TextView = view.findViewById(R.id.tvNo)
            val tvAyahArabic: TextView = view.findViewById(R.id.tvAyah)
            val tvVerse: TextView = view.findViewById(R.id.tvVerse)
            val ibHizb: ImageButton = view.findViewById(R.id.ibChapter)
            val llHizb: LinearLayout = view.findViewById(R.id.llHizb)
            val vLine: View = view.findViewById(R.id.view)

            init
            {
                view.setOnClickListener(this)
                ibHizb.setOnClickListener(this)
                llHizb.setOnClickListener(this)
            }

            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun onClick(v: View?)
            {
                when (v?.id)
                {
                    R.id.ibChapter,
                    R.id.parent ->
                    {
                        if (llHizb.visibility == View.VISIBLE)
                        {
                            visibleJuzPosition = -1
                            llHizb.visibility = View.GONE
                            vLine.visibility = View.VISIBLE
                            ibHizb.setImageDrawable(
                                ContextCompat.getDrawable(
                                    v.context,
                                    R.drawable.arrow_down
                                )
                            )
                        } else
                        {
                            visibleJuzPosition = adapterPosition
                            notifyDataSetChanged()
                        }
                    }

                    else ->
                    {
                        mainActivity.showPremiumDialogOrAd {
                            val bundle = v?.tag as Bundle
                            val surahIndex = bundle.getInt("surahIndex")
                            val ayahIndex = bundle.getInt("ayahIndex")
                            val transaction = mainActivity.supportFragmentManager.beginTransaction()
                            transaction.replace(
                                R.id.container,
                                QuranFragment.newInstance(surahIndex, ayahIndex),
                                "quran"
                            );
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            fun printHizb(): Boolean
            {
                ayahsByJuz[adapterPosition]?.distinctBy { it.hizbQuarter }?.forEach { ayah ->
                    for (surah in surahs)
                    {
                        if (surah.ayahs.contains(ayah))
                        {
                            val view = LayoutInflater.from(llHizb.context)
                                .inflate(R.layout.row_quran_hizb, llHizb, false)

                            val tvHizb: TextView = view.findViewById(R.id.tvNo)
                            val tvAyahArabic: TextView = view.findViewById(R.id.tvAyah)
                            val tvVerse: TextView = view.findViewById(R.id.tvVerse)

                            tvHizb.background = when (ayah.hizbQuarter % 4)
                            {
                                0 -> ContextCompat.getDrawable(view.context, R.drawable.circle_4)
                                1 ->
                                {
                                    tvHizb.text = "${(ayah.hizbQuarter + 3) / 4}"
                                    ContextCompat.getDrawable(view.context, R.drawable.circle_1)
                                }

                                2 -> ContextCompat.getDrawable(view.context, R.drawable.circle_2)
                                else -> ContextCompat.getDrawable(view.context, R.drawable.circle_3)
                            }
                            tvAyahArabic.text = ayah.text
                            tvVerse.text = "Surah ${surah.englishName}, Ayah ${ayah.numberInSurah}"

                            llHizb.addView(view)

                            view.tag = Bundle().apply {
                                putInt("surahIndex", surah.number - 1)
                                putInt("ayahIndex", ayah.numberInSurah - 1)
                            }
                            view.setOnClickListener(this@ViewHolder)
                            break
                        }
                    }
                }
                return true
            }
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = QuranParaPagerFragment()
    }
}