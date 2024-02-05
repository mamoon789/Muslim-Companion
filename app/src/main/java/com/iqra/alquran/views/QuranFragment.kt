package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Quran.Data.Surah
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.utils.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*

@Suppress("UNCHECKED_CAST")
class QuranFragment : Fragment(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
    lateinit var sharedPreferences: SharedPreferences
    private var bookmarksList: MutableList<String>? = null

    lateinit var btTranslation: Button
    lateinit var btPlayback: ToggleButton
    lateinit var btBookmark: ToggleButton

    lateinit var surahs: MutableList<Surah>
    lateinit var edition: String

    private lateinit var tts: TextToSpeech
    private lateinit var wv: WebView
    private lateinit var sb: SeekBar

    var zoom = Constants.CURRENT_ZOOM

    var audioRunning = false
    var translationActive = false

    var mp: MediaPlayer? = null
    var surahIndex = 0
    var ayahIndex = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        surahs = (activity as MainActivity).surahs
        edition = (activity as MainActivity).language
        arguments?.let {
            surahIndex = it.getInt("surahIndex", 0)
            ayahIndex = it.getInt("ayahIndex", 0)
        }

        sharedPreferences = activity!!.getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        bookmarksList = sharedPreferences.getStringSet(Constants.KEY_BOOKMARKS, setOf<String>())
            ?.toMutableList()

        val view = inflater.inflate(R.layout.fragment_quran_page, container, false)
        wv = view.findViewById(R.id.wv)
        sb = view.findViewById(R.id.sb)
        btPlayback = view.findViewById(R.id.btPlayback)
        btTranslation = view.findViewById(R.id.btTranslation)
        btBookmark = view.findViewById(R.id.btBookmark)

        wv.settings.javaScriptEnabled = true
//        wv.settings.textZoom = zoom
//        sb.progress = zoom

        btBookmark.setOnClickListener {
            val bookmarkId = "surah" + surahIndex + "_ayah" + ayahIndex
            if (bookmarksList!!.contains(bookmarkId))
            {
                bookmarksList!!.remove(bookmarkId)
            } else
            {
                bookmarksList!!.add(bookmarkId)
            }

            val editor = sharedPreferences.edit()
            editor.putStringSet(Constants.KEY_BOOKMARKS, bookmarksList!!.toSet())
            editor.apply()
        }

        btTranslation.setOnClickListener {
            translationActive = !translationActive

            mp?.release()
            mp = null
            tts.stop()

            print()
        }

        btPlayback.setOnClickListener {
            if (!(activity as MainActivity).checkInternetConnection())
            {
                btPlayback.isChecked = false
                return@setOnClickListener
            }
            if (!audioRunning)
            {
                (activity as MainActivity).showCustomDialog(R.layout.progress_dialog_audio)
                audioRunning = true
                mp = MediaPlayer()
                mp?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mp?.setDataSource(
                    surahs[surahIndex].ayahs[ayahIndex].audio.replace(
                        "https",
                        "http"
                    )
                )
                mp?.prepareAsync()
                mp?.setOnPreparedListener(this@QuranFragment)
                mp?.setOnCompletionListener(this@QuranFragment)
            } else
            {
                audioRunning = false
                mp?.release()
                mp = null
                tts.stop()
            }
        }

        tts = TextToSpeech(activity?.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS)
            {
//                val enVoices: HashSet<Voice> = HashSet()
//                for (voice in tts.voices) {
//                    if (voice.name.contains(edition) && voice.name.contains("en-us",true)) {
//                        enVoices.add(voice)
//                    } else if (voice.name.contains(edition) && voice.name.contains("ur-pk",true)) {
//                        enVoices.add(voice)
//                    }
//                }
//                tts.voice = enVoices.toArray()[enVoices.size - 3] as Voice?
//                tts.voice = enVoices.toArray()[0] as Voice?
//                tts.language = Locale.UK
                tts.setSpeechRate(1.0f)
                tts.setPitch(1.0f)
            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener()
        {
            override fun onStart(utteranceId: String?)
            {
            }

            override fun onDone(utteranceId: String?)
            {
                if (updateCurrentAyahIndex())
                {
                    CoroutineScope(Dispatchers.Main).launch {
                        wv.loadUrl("javascript:scrollToAyahAndTranslation('surah' + $surahIndex + '_ayah' + $ayahIndex)")
                    }
                } else
                {
                    btPlayback.isChecked = false
                    audioRunning = false
                    mp?.release()
                    mp = null
                    tts.stop()
                }
            }

            override fun onError(utteranceId: String?)
            {
            }
        })

        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            )
            {
                zoom = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar)
            {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar)
            {
//                mp?.release()
//                mp = null
//                tts.stop()
                wv.settings.textZoom = zoom
//                print()
            }
        })

        print()

        wv.webViewClient = object : WebViewClient()
        {
            override fun onLoadResource(view: WebView?, url: String?)
            {
                (activity as MainActivity).showCustomDialog(R.layout.progress_dialog)
            }

            override fun onPageFinished(view: WebView, weburl: String)
            {
                try
                {
                    val juzNo = surahs[surahIndex].ayahs[ayahIndex].juz
                    wv.loadUrl("javascript:setJuzLoaded(false)")
                    wv.loadUrl("javascript:setMinJuzSelected(\"$juzNo\")")
                    wv.loadUrl("javascript:setMaxJuzSelected(\"$juzNo\")")

//                    Handler().postDelayed({
//                        (activity as MainActivity).hideDialog()
                    if (translationActive)
                    {
                        Handler().postDelayed({
                            (activity as MainActivity).hideDialog()
                            wv.loadUrl("javascript:scrollToAyahAndTranslation('surah' + $surahIndex + '_ayah' + $ayahIndex)")
                        }, 1000)
                    } else
                    {
                        (activity as MainActivity).hideDialog()
                        wv.loadUrl("javascript:scrollToAyah('surah' + $surahIndex + '_ayah' + $ayahIndex)")
                    }
//                    }, 1000)
                    Handler().postDelayed({
                        wv.loadUrl("javascript:setJuzLoaded(true)")
//                        wv.loadUrl("javascript:scrollListener()")
                    }, 2000)
                } catch (exception: Exception)
                {
                    exception.printStackTrace()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            )
            {
                super.onReceivedError(view, request, error)
            }
        }

        wv.webChromeClient = object : WebChromeClient()
        {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean
            {
                Log.d("WebView", consoleMessage.message())

                val id = consoleMessage.message()
                if (id.contains("juz"))
                {
                    var juzNo = id.substring(3, id.indexOf(' ')).toInt()
                    if (id.contains("next") && juzNo < 30)
                    {
                        juzNo += 1
                        var juz = Utility.getHtmlBody(activity, "$juzNo.html", translationActive)
                        juz = juz.replace("\"", "&quot;")
                        wv.loadUrl("javascript:insertEndHTML(\"$juz\")")
                        wv.loadUrl("javascript:setMaxJuzSelected(\"$juzNo\")")
                    } else if (id.contains("previous") && juzNo > 1)
                    {
                        juzNo -= 1
                        var juz = Utility.getHtmlBody(activity, "$juzNo.html", translationActive)
                        juz = juz.replace("\"", "&quot;")
                        wv.loadUrl("javascript:insertStartHTML(\"$juz\")")
                        wv.loadUrl("javascript:setMinJuzSelected(\"$juzNo\")")
                    }
                    wv.loadUrl("javascript:setJuzLoaded(true)")
                } else if (id.contains("surah"))
                {
                    val editor = sharedPreferences.edit()
                    editor.putString(Constants.KEY_LAST_READ, id)
                    editor.apply()

                    btBookmark.isChecked = bookmarksList!!.contains(id)

                    surahIndex = id.substring(5, id.indexOf('_')).toInt()
                    ayahIndex = id.substring(id.indexOf('_') + 5).toInt()

                    if (audioRunning)
                    {
                        mp?.release()
                        mp = null
                        tts.stop()

                        mp = MediaPlayer()
                        mp?.setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        mp?.setDataSource(
                            surahs[surahIndex].ayahs[ayahIndex].audio.replace(
                                "https",
                                "http"
                            )
                        )
                        mp?.prepareAsync()
                        mp?.setOnPreparedListener(this@QuranFragment)
                        mp?.setOnCompletionListener(this@QuranFragment)
                    }
                }
                return true
            }
        }
        return view
    }

    override fun onPause()
    {
        super.onPause()
        btPlayback.isChecked = false
        audioRunning = false
        mp?.release()
        mp = null
        tts.stop()
    }

    private fun updateCurrentAyahIndex(): Boolean
    {
        val lastAyahIndex = surahs[surahIndex].ayahs.size - 1
        val lastSurahIndex = surahs.size - 1
        if (ayahIndex < lastAyahIndex)
        {
            ayahIndex++
            return true
        } else if (surahIndex < lastSurahIndex)
        {
            ayahIndex = 0
            surahIndex++
            return true
        } else
        {
            return false
        }
    }

    private fun print()
    {
        val htmlStart = "<html>\n"
        val headStart = "<head>\n"
        val style = "<style>\n" +
                "@import url('https://fonts.googleapis.com/css2?family=Ubuntu&family=Open+Sans&family=Poppins&family=Merriweather&family=Lora&display=swap');\n" +
                "@import url('https://fonts.googleapis.com/css2?family=Tajawal&family=Amiri:wght@700&family=Almarai&family=Lateef&family=Scheherazade:wght@700&family=Harmattan&family=Mirza:wght@500&display=swap');\n" +
                "@font-face {\n" +
                "   font-family: 'indopak_font';\n" +
                "   src: url('file:///android_asset/indopak_font.woff');\n" +
                "}\n" +
                "*{\n" +
                "   text-align: justify;\n" +
                "   text-align-last: center;\n" +
                "   scroll-behavior: smooth;\n" +
                "   background-color: #ffffff;\n" +
                "}\n" +
                ".ar {\n" +
                "   font-family: ${Constants.CURRENT_SCRIPT_FONT};\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   font-size: 175%;\n" +
                "   direction: rtl;\n" +
                "}\n" +
                ".ur {\n" +
                "   font-family: ${Constants.CURRENT_TRANSLATION_FONT};\n" +
                "   font-size: 150%;\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   direction: rtl;\n" +
                "}\n" +
                ".en {\n" +
                "   font-family: ${Constants.CURRENT_TRANSLATION_FONT};\n" +
                "   font-size: 100%;\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   direction: ltr;\n" +
                "}\n" +
                "h2 {\n" +
                "   font-family: ${Constants.CURRENT_SCRIPT_FONT};\n" +
                "   font-size: 120%;\n" +
                "}\n" +
                ".containerAyah {\n" +
                "   border: 1px;\n" +
                "   border-style: solid;\n" +
                "   border-color: black;\n" +
                "   border-radius: 50%;\n" +
                "   font-size: 1.0rem;\n" +
                "   display: inline-flex;\n" +
                "   justify-content: center;\n" +
                "   align-items: center;\n" +
                "   padding: 0.5em;\n" +
                "   width: 1em;\n" +
                "   height: 1em;\n" +
                "}\n" +
                ".containerRuku {\n" +
                "   position: relative;\n" +
                "   display: inline-flex;\n" +
                "}\n" +
                ".ruku {\n" +
                "  position: absolute;\n" +
                "  top:-75%;\n" +
                "  left: 0%;\n" +
                "  right:0%;\n" +
                "  background-color:transparent;\n" +
                "  font-size: 1.0rem;\n" +
                "}\n" +
                "</style>\n"
        val script = "<script>\n" +

                "var ayahSelected = null;\n" +

                "var minJuzSelected = null;\n" +
                "var maxJuzSelected = null;\n" +
                "var juzLoaded = false;\n" +

                "function setMinJuzSelected(juz){ minJuzSelected = juz }\n" +
                "function setMaxJuzSelected(juz){ maxJuzSelected = juz }\n" +
                "function setJuzLoaded(flag){ juzLoaded = flag }\n" +

                "window.onscroll = function() {\n" +
                "if(juzLoaded)\n" +
                "scrollListener()\n" +
                "}\n" +

                "function scrollListener() {\n" +
                "var winScroll = document.body.scrollTop || document.documentElement.scrollTop;\n" +
                "var height = document.documentElement.scrollHeight - window.innerHeight;\n" +
                "var scrolled = (winScroll / height) * 100;\n" +
//                "console.log('scrolled: '+scrolled)\n" +
                "if(scrolled <= 1){\n" +
                "setJuzLoaded(false)\n" +
                "console.log('juz' + minJuzSelected, 'previous')\n" +
                "}\n" +
                "else if(scrolled >= 99){\n" +
                "setJuzLoaded(false)\n" +
                "console.log('juz' + maxJuzSelected, 'next')\n" +
                "}\n" +
                "}\n" +

//                "function scrollListener() {\n" +
//                "var winScroll = document.body.scrollTop || document.documentElement.scrollTop;\n" +
//                "var height = document.documentElement.scrollHeight - window.innerHeight;\n" +
//                "var scrolled = (winScroll / height) * 100;\n" +
////                "console.log('scrolled: '+scrolled)\n" +
//                "if(scrolled <= 1 && juzLoaded == false){\n" +
//                "console.log('juz' + minJuzSelected, 'previous')\n" +
//                "juzLoaded = true\n" +
//                "}\n" +
//                "else if(scrolled >= 99 && juzLoaded == false){\n" +
//                "console.log('juz' + maxJuzSelected, 'next')\n" +
//                "juzLoaded = true\n" +
//                "}\n" +
//                "}\n" +

                "function insertEndHTML(juz){\n" +
                "document.body.insertAdjacentHTML(\"beforeend\", juz);\n" +
                "}\n" +

                "function insertStartHTML(juz){\n" +
                "document.body.insertAdjacentHTML(\"afterbegin\", juz);\n" +
                "}\n" +

                "function highlightAyah(id) {\n" +
                "if(ayahSelected != null) unHighlightAyah(ayahSelected);\n" +
                "document.getElementById(id).style.color = 'purple';\n" +
                "if(document.getElementById(id).children[0].children.length > 0)\n" +
                "document.getElementById(id).children[0].children[0].style.borderColor = 'purple';\n" +
                "else\n" +
                "document.getElementById(id).children[0].style.borderColor = 'purple';\n" +
                "ayahSelected = id;\n" +
                "console.log(id);\n" +
                "}\n" +

                "function unHighlightAyah(id) {\n" +
                "document.getElementById(id).style.color = 'black';\n" +
                "if(document.getElementById(id).children[0].children.length > 0)\n" +
                "document.getElementById(id).children[0].children[0].style.borderColor = 'black';\n" +
                "else\n" +
                "document.getElementById(id).children[0].style.borderColor = 'black';\n" +
                "}\n" +

                "function highlightAyahAndTranslation(id) {\n" +
                "if(ayahSelected != null) unHighlightAyahAndTranslation(ayahSelected);\n" +
                "document.getElementById(id).style.color = 'purple';\n" +
                "if(document.getElementById(id).children[0].children.length > 0)\n" +
                "document.getElementById(id).children[0].children[0].style.borderColor = 'purple';\n" +
                "else\n" +
                "document.getElementById(id).children[0].style.borderColor = 'purple';\n" +
                "document.getElementById(id + '_tr').style.color = 'purple';\n" +
                "ayahSelected = id;\n" +
                "console.log(id);\n" +
                "}\n" +

                "function unHighlightAyahAndTranslation(id) {\n" +
                "document.getElementById(id).style.color = 'black';\n" +
                "if(document.getElementById(id).children[0].children.length > 0)\n" +
                "document.getElementById(id).children[0].children[0].style.borderColor = 'black';\n" +
                "else\n" +
                "document.getElementById(id).children[0].style.borderColor = 'black';\n" +
                "document.getElementById(id + '_tr').style.color = 'black';\n" +
                "}\n" +

                "function scrollToAyah(id){\n" +
                "document.getElementById(id).scrollIntoView();\n" +
                "highlightAyah(id);\n" +
                "}\n" +

                "function scrollToAyahAndTranslation(id){\n" +
                "document.getElementById(id).scrollIntoView();\n" +
                "highlightAyahAndTranslation(id);\n" +
                "}\n" +
                "</script>\n"

        val headEnd = "</head>\n"
        val bodyStart = "<body>\n"
        var body = ""
        val bodyEnd = "</body>\n"
        val htmlEnd = "</html>\n"

        val juzNo = surahs[surahIndex].ayahs[ayahIndex].juz
        body = Utility.getHtmlBody(activity, "$juzNo.html", translationActive)

        wv.loadDataWithBaseURL(
            null,
            htmlStart + headStart + style + script + headEnd + bodyStart + body + bodyEnd + htmlEnd,
            "text/html",
            "UTF-8", null
        ).toString()
    }

    companion object
    {
        @JvmStatic
        fun newInstance(
            surahIndex: Int,
            ayahIndex: Int
        ) =
            QuranFragment().apply {
                arguments = Bundle().apply {
                    putInt("surahIndex", surahIndex)
                    putInt("ayahIndex", ayahIndex)
                }
            }
    }

    override fun onPrepared(mp1: MediaPlayer?)
    {
        (activity as MainActivity).hideDialog()
        mp1?.start()
    }

    override fun onCompletion(mp1: MediaPlayer?)
    {
        mp1?.release()
        if (translationActive)
        {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "a")
            }
            tts.speak(
                surahs[surahIndex].ayahs[ayahIndex].translation,
                TextToSpeech.QUEUE_FLUSH,
                params,
                "MyUniqueUtteranceId"
            )
        } else if (updateCurrentAyahIndex())
        {
            wv.loadUrl("javascript:scrollToAyah('surah' + $surahIndex + '_ayah' + $ayahIndex)")
        } else
        {
            btPlayback.isChecked = false
            audioRunning = false
            mp?.release()
            mp = null
            tts.stop()
        }
    }

    override fun onStop()
    {
        super.onStop()
        if (!sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false))
            (activity as MainActivity).loadAd()
        mp?.release()
        mp = null
        tts.stop()
    }
}