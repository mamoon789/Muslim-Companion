package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.rvadapter.AdmobNativeAdAdapter
import com.google.gson.Gson
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Hadith
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class HadithFragment : Fragment()
{
    private lateinit var mainActivity: MainActivity
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tts: TextToSpeech
    private lateinit var rvHadiths: RecyclerView
    private lateinit var bookmarks: MutableList<Hadith.Hadiths.Data>
    private lateinit var adapter: Adapter
    private lateinit var adapterAd: AdmobNativeAdAdapter
    private var tbPlaybackCopy: ToggleButton? = null
    private var hadiths = listOf<Hadith.Hadiths.Data>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainActivity = activity as MainActivity
        tts = TextToSpeech(mainActivity, {}, "com.google.android.tts")
        sharedPreferences =
            mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE).apply {
                bookmarks = getStringSet(Constants.KEY_HADITH_BOOKMARKS, emptySet())!!
                    .map {
                        Gson().fromJson(it, Hadith.Hadiths.Data::class.java)
                    }.toMutableList()

            }

        arguments?.let {
            val bookSlug = it.getString("bookSlug", "")
            val chapter = it.getString("chapter", "")
            val hadithEnglish = it.getString("hadithEnglish", "")
            viewModel.getHadiths(book = bookSlug, chapter = chapter, hadithEnglish = hadithEnglish)
        }

        adapter = Adapter()
        adapterAd = AdmobNativeAdAdapter.Builder.with(
            if (BuildConfig.DEBUG) Constants.NATIVE_AD_ID else BuildConfig.NATIVE_AD_ID,
            adapter,
            "medium"
        )
            .adItemInterval(4)
            .build()

        viewModel.hadiths.observe(this) {
            mainActivity.hideDialog()
            if (it.data != null)
            {
                mainActivity.showPremiumDialogOrAd()
                hadiths = it.data.hadiths.data
                adapter.notifyDataSetChanged()
            } else if (it.message != null)
            {
                mainActivity.showSnackBar(it.message, it.message == Constants.MSG_CONNECT_INTERNET)
            } else
            {
                mainActivity.showCustomDialog(R.layout.dialog_progress)
            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener()
        {
            override fun onStart(utteranceId: String?)
            {
            }

            override fun onDone(utteranceId: String?)
            {
                tts.stop()
                tbPlaybackCopy?.isChecked = false
            }

            @Deprecated("Deprecated")
            override fun onError(utteranceId: String?)
            {
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_hadith, container, false)
        rvHadiths = view.findViewById(R.id.rvHadiths)

        if (!sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false))
        {
            rvHadiths.adapter = adapterAd
        } else
        {
            rvHadiths.adapter = adapter
        }
        rvHadiths.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        return view
    }

    override fun onPause()
    {
        super.onPause()
        tts.stop()
        tbPlaybackCopy?.isChecked = false
    }

    override fun onDestroy()
    {
        super.onDestroy()
        tts.shutdown()
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_hadith, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.apply {
                hadiths[position].let { hadith ->
                    tvNo.text = hadith.hadithNumber
                    tvStatus.text = hadith.status
                    tvNarration.text = hadith.englishNarrator
                    tvHadith.text = hadith.hadithEnglish
                    tbBookmark.isChecked = bookmarks.contains(hadith)

                    tbPlayback.setOnClickListener {
                        if (!tbPlayback.isChecked)
                        {
                            tts.stop()
                            return@setOnClickListener
                        }

                        val result = tts.setLanguage(Locale(BuildConfig.EDITION))
                        if (result == TextToSpeech.LANG_AVAILABLE)
                        {
                            tbPlaybackCopy = tbPlayback
                            val params = Bundle().apply {
                                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "a")
                            }
                            tts.speak(
                                "${hadith.englishNarrator} ${hadith.hadithEnglish}",
                                TextToSpeech.QUEUE_FLUSH,
                                params,
                                "MyUniqueUtteranceId"
                            )
                            return@setOnClickListener
                        }

                        mainActivity.showCustomDialog(
                            title = "Alert",
                            message = "This device doesn't support ${Locale(BuildConfig.EDITION).displayName}. " +
                                    "To listen, download the ${Locale(BuildConfig.EDITION).displayName} voice data.",
                            positiveTxt = "Download",
                            negativeTxt = "Skip",
                            positiveListener = { _, _ ->
                                mainActivity.hideDialog()
                                Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                                    setPackage("com.google.android.tts")
                                    startActivity(this)
                                }
                                tbPlayback.isChecked = false
                            },
                            negativeListener = { _, _ ->
                                mainActivity.hideDialog()
                                tbPlayback.isChecked = false
                            })
                    }

                    tbBookmark.setOnClickListener {
                        if (tbBookmark.isChecked)
                        {
                            bookmarks.add(hadith)
                        } else
                        {
                            bookmarks.remove(hadith)
                        }
                        sharedPreferences.edit().apply {
                            putStringSet(
                                Constants.KEY_HADITH_BOOKMARKS,
                                bookmarks.map { Gson().toJson(it) }.toSet()
                            )
                            apply()
                        }
                    }

                    ibCopy.setOnClickListener {
                        val clipboard =
                            mainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Hadith",
                            "${hadith.englishNarrator} \n${hadith.hadithEnglish}"
                        )
                        clipboard.setPrimaryClip(clip)
                    }

                    ibShare.setOnClickListener {
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${hadith.englishNarrator} \n${hadith.hadithEnglish} \n\n${
                                    getString(
                                        R.string.share_data
                                    )
                                }"
                            )
                            startActivity(Intent.createChooser(this, getString(R.string.share)))
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int
        {
            return hadiths.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
        {
            val tvNo: TextView = view.findViewById(R.id.tvNo)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val tvNarration: TextView = view.findViewById(R.id.tvNarration)
            val tvHadith: TextView = view.findViewById(R.id.tvHadith)
            val tbPlayback: ToggleButton = view.findViewById(R.id.tbPlayback)
            val tbBookmark: ToggleButton = view.findViewById(R.id.tbBookmark)
            val ibCopy: ImageButton = view.findViewById(R.id.ibCopy)
            val ibShare: ImageButton = view.findViewById(R.id.ibShare)
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance(bookSlug: String, chapter: String, hadithEnglish: String = "") =
            HadithFragment().apply {
                arguments = Bundle().apply {
                    putString("bookSlug", bookSlug)
                    putString("chapter", chapter)
                    putString("hadithEnglish", hadithEnglish)
                }
            }
    }
}