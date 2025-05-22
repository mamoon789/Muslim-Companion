package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Dhikr
import com.iqra.alquran.network.models.Quran.Data.Surah
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.utils.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class QuranFragment : Fragment(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
    lateinit var wv: WebView
    lateinit var sb: SeekBar
    lateinit var btTranslation: Button
    lateinit var btPlayback: ToggleButton
    lateinit var btBookmark: ToggleButton

    lateinit var tts: TextToSpeech
    var mp: MediaPlayer? = null

    lateinit var mainActivity: MainActivity
    lateinit var sharedPreferences: SharedPreferences
    var bookmarksList: MutableList<String>? = null

    lateinit var surahs: MutableList<Surah>
    var surahIndex = 0
    var ayahIndex = 0
    var audioRunning = false
    var translationActive = false
    var rukuTopPadding = -25
    var ayahContainerSize = 0.85
    var sbListener = object : SeekBar.OnSeekBarChangeListener
    {
        var progress = 0
        override fun onProgressChanged(
            seekBar: SeekBar, progress: Int,
            fromUser: Boolean
        )
        {
            this.progress = progress
        }

        override fun onStartTrackingTouch(seekBar: SeekBar)
        {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar)
        {
            wv.settings.textZoom = progress
            rukuTopPadding = -25 - (progress - 100) / 5
            ayahContainerSize = 0.85 + (progress - 100) / 50
            wv.loadUrl("javascript:updateRukuContainer($rukuTopPadding, $ayahContainerSize)")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_quran_page, container, false)
        init(view)
        listeners()
        print()
        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init(view: View)
    {
        view.apply {
            wv = findViewById<WebView?>(R.id.wv).apply { settings.javaScriptEnabled = true }
            sb = findViewById(R.id.sb)
            btPlayback = findViewById(R.id.btPlayback)
            btTranslation = findViewById(R.id.btTranslation)
            btBookmark = findViewById(R.id.btBookmark)
        }

        mainActivity = activity as MainActivity
        sharedPreferences =
            mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE).apply {
                bookmarksList =
                    getStringSet(Constants.KEY_QURAN_BOOKMARKS, setOf<String>())?.toMutableList()
            }

        surahs = mainActivity.surahs
        arguments?.run {
            surahIndex = getInt("surahIndex", 0)
            ayahIndex = getInt("ayahIndex", 0)
        }

        tts = TextToSpeech(requireContext(), {}, "com.google.android.tts")
    }

    private fun print()
    {
        val htmlStart = "<html>\n"
        val headStart = "<head>\n"

        val style = "<style>\n" +
                "@import url('https://fonts.googleapis.com/css2?family=Arial&family=Scheherazade:wght@700&family=Lateef&family=Mirza&display=swap');\n" +
                "@import url('https://fonts.googleapis.com/css2?family=Roboto&family=Open+Sans&family=Poppins&family=Georgia&family=Times+New+Roman&display=swap');\n" +
                "@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+Devanagari&family=Lohit+Devanagari&family=Samarkan&family=Hind&family=Kalam&display=swap');\n" +
                "@font-face {\n" +
                "   font-family: 'indopak_font';\n" +
                "   src: url('file:///android_asset/font.woff');\n" +
                "}\n" +
                "*{\n" +
                "   text-align: justify;\n" +
                "   text-align-last: center;\n" +
                "   scroll-behavior: smooth;\n" +
                "   background-color: #ffffff;\n" +
                "}\n" +
                ".ar {\n" +
                "   font-family: ${Constants.CURRENT_SCRIPT_FONT};\n" +
                "   font-size: 175%;\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   direction: rtl;\n" +
                "}\n" +
                ".ur {\n" +
                "   font-family: ${Constants.CURRENT_TRANSLATION_FONT};\n" +
                "   font-size: 150%;\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   direction: rtl;\n" +
                "}\n" +
                ".hi {\n" +
                "   font-family: ${Constants.CURRENT_TRANSLATION_FONT};\n" +
                "   font-size: 125%;\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   direction: ltr;\n" +
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
                "   font-size: 0.5em;\n" +
                "   display: inline-flex;\n" +
                "   justify-content: center;\n" +
                "   align-items: center;\n" +
                "   padding: 0.5em;\n" +
                "   width: ${ayahContainerSize}em;\n" +
                "   height: ${ayahContainerSize}em;\n" +
                "}\n" +
                ".containerRuku {\n" +
                "   position: relative;\n" +
                "   display: inline-flex;\n" +
                "}\n" +
                ".ruku {\n" +
                "  position: absolute;\n" +
                "  top: $rukuTopPadding;\n" +
                "  left: 0;\n" +
                "  right:0;\n" +
                "  background-color:transparent;\n" +
                "  font-size: 1.0rem;\n" +
                "}\n" +
                "</style>\n"

        val script = "<script>\n" +
                "var ayahSelected = null;\n" +
                "var minJuzSelected = null;\n" +
                "var maxJuzSelected = null;\n" +
                "var juzLoaded = false;\n" +

                "function updateRukuContainer(rukuTopPadding, ayahContainerSize) {\n" +
                "   var ruku = document.getElementsByClassName('ruku');\n" +
                "   for(i = 0; i < ruku.length; i++) {\n" +
                "       ruku[i].style.top = rukuTopPadding\n" + // -25 to -35
                "   }\n" +
                "   var ayah = document.getElementsByClassName('containerAyah');\n" +
                "   for(i = 0; i < ayah.length; i++) {\n" +
                "       ayah[i].style.width = ayahContainerSize + 'em'\n" + // 0.85em to 0.95
                "       ayah[i].style.height = ayahContainerSize + 'em'\n" +
                "   }\n" +
                "}\n" +

                "function setMinJuzSelected(juz){ minJuzSelected = juz }\n" +
                "function setMaxJuzSelected(juz){ maxJuzSelected = juz }\n" +
                "function setJuzLoaded(flag){ juzLoaded = flag }\n" +

                "window.onscroll = function() {\n" +
                "   if(juzLoaded) scrollListener()\n" +
                "}\n" +

                "function scrollListener() {\n" +
                "   var winScroll = document.body.scrollTop || document.documentElement.scrollTop;\n" +
                "   var height = document.documentElement.scrollHeight - window.innerHeight;\n" +
                "   var scrolled = (winScroll / height) * 100;\n" +
                "   if(scrolled <= 1){\n" +
                "       setJuzLoaded(false)\n" +
                "       console.log('juz' + minJuzSelected, 'previous')\n" +
                "   }\n" +
                "   else if(scrolled >= 99){\n" +
                "       setJuzLoaded(false)\n" +
                "       console.log('juz' + maxJuzSelected, 'next')\n" +
                "   }\n" +
                "}\n" +

                "function insertEndHTML(juz){\n" +
                "   document.body.insertAdjacentHTML(\"beforeend\", juz);\n" +
                "}\n" +

                "function insertStartHTML(juz){\n" +
                "   document.body.insertAdjacentHTML(\"afterbegin\", juz);\n" +
                "}\n" +

                "function highlightAyah(id) {\n" +
                "   if(ayahSelected != null) unHighlightAyah(ayahSelected);\n" +
                "   document.getElementById(id).style.color = 'purple';\n" +
                "   if(document.getElementById(id).children[0].children.length > 0)\n" +
                "       document.getElementById(id).children[0].children[0].style.borderColor = 'purple';\n" +
                "   else\n" +
                "      document.getElementById(id).children[0].style.borderColor = 'purple';\n" +
                "   ayahSelected = id;\n" +
                "   console.log(id);\n" +
                "}\n" +

                "function unHighlightAyah(id) {\n" +
                "   document.getElementById(id).style.color = 'black';\n" +
                "   if(document.getElementById(id).children[0].children.length > 0)\n" +
                "       document.getElementById(id).children[0].children[0].style.borderColor = 'black';\n" +
                "   else\n" +
                "       document.getElementById(id).children[0].style.borderColor = 'black';\n" +
                "}\n" +

                "function highlightAyahAndTranslation(id) {\n" +
                "   if(ayahSelected != null) unHighlightAyahAndTranslation(ayahSelected);\n" +
                "   document.getElementById(id).style.color = 'purple';\n" +
                "   if(document.getElementById(id).children[0].children.length > 0)\n" +
                "       document.getElementById(id).children[0].children[0].style.borderColor = 'purple';\n" +
                "   else\n" +
                "       document.getElementById(id).children[0].style.borderColor = 'purple';\n" +
                "   document.getElementById(id + '_tr').style.color = 'purple';\n" +
                "   ayahSelected = id;\n" +
                "   console.log(id);\n" +
                "}\n" +

                "function unHighlightAyahAndTranslation(id) {\n" +
                "   document.getElementById(id).style.color = 'black';\n" +
                "   if(document.getElementById(id).children[0].children.length > 0)\n" +
                "       document.getElementById(id).children[0].children[0].style.borderColor = 'black';\n" +
                "   else\n" +
                "       document.getElementById(id).children[0].style.borderColor = 'black';\n" +
                "   document.getElementById(id + '_tr').style.color = 'black';\n" +
                "}\n" +

                "function scrollToAyah(id){\n" +
                "   document.getElementById(id).scrollIntoView();\n" +
                "   highlightAyah(id);\n" +
                "}\n" +

                "function scrollToAyahAndTranslation(id){\n" +
                "   document.getElementById(id).scrollIntoView();\n" +
                "   highlightAyahAndTranslation(id);\n" +
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

        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.SECONDS.toMillis(0.5.toLong()))
            withContext(Dispatchers.Main) {
                sb.progress = Constants.CURRENT_ZOOM
                sbListener.onProgressChanged(sb, Constants.CURRENT_ZOOM, false)
                sbListener.onStopTrackingTouch(sb)
            }
        }
    }

    private fun listeners()
    {
        wv.webViewClient = object : WebViewClient()
        {
            override fun onLoadResource(view: WebView?, url: String?)
            {
                mainActivity.showCustomDialog(R.layout.dialog_progress)
            }

            override fun onPageFinished(view: WebView, weburl: String)
            {
                try
                {
                    val juzNo = surahs[surahIndex].ayahs[ayahIndex].juz
                    wv.loadUrl("javascript:setJuzLoaded(false)")
                    wv.loadUrl("javascript:setMinJuzSelected($juzNo)")
                    wv.loadUrl("javascript:setMaxJuzSelected($juzNo)")

                    CoroutineScope(Dispatchers.Main).launch {
                        if (translationActive)
                        {
                            delay(1000)
                            mainActivity.hideDialog()
                            wv.loadUrl("javascript:scrollToAyahAndTranslation('surah' + $surahIndex + '_ayah' + $ayahIndex)")

                        } else
                        {
                            mainActivity.hideDialog()
                            wv.loadUrl("javascript:scrollToAyah('surah' + $surahIndex + '_ayah' + $ayahIndex)")
                        }
                        delay(2000)
                        wv.loadUrl("javascript:setJuzLoaded(true)")
                    }
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
                val id = consoleMessage.message().apply { Log.d("onConsoleMessage", this) }
                if (id.contains("juz"))
                {
                    var juzNo = id.substring(3, id.indexOf(' ')).toInt()
                    if (id.contains("next") && juzNo < 30)
                    {
                        val juz =
                            Utility.getHtmlBody(activity, "${++juzNo}.html", translationActive)
                                .replace("\"", "&quot;")
                        wv.loadUrl("javascript:insertEndHTML(\"$juz\")")
                        wv.loadUrl("javascript:setMaxJuzSelected($juzNo)")
                    } else if (id.contains("previous") && juzNo > 1)
                    {
                        val juz =
                            Utility.getHtmlBody(activity, "${--juzNo}.html", translationActive)
                                .replace("\"", "&quot;")
                        wv.loadUrl("javascript:insertStartHTML(\"$juz\")")
                        wv.loadUrl("javascript:setMinJuzSelected($juzNo)")
                    }
                    wv.loadUrl("javascript:updateRukuContainer($rukuTopPadding, $ayahContainerSize)")
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

        sb.setOnSeekBarChangeListener(sbListener)

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

            @Deprecated("Deprecated")
            override fun onError(utteranceId: String?)
            {
            }
        })

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
            editor.putStringSet(Constants.KEY_QURAN_BOOKMARKS, bookmarksList!!.toSet())
            editor.apply()
        }

        btTranslation.setOnClickListener {
            translationActive = !translationActive

            mp?.release()
            mp = null
            tts.stop()

            val result = tts.setLanguage(Locale(Constants.CURRENT_TRANSLATION))
            if (!translationActive || result == TextToSpeech.LANG_AVAILABLE)
            {
                print()
                return@setOnClickListener
            }

            mainActivity.showCustomDialog(
                title = "For translation, install the ${Locale(Constants.CURRENT_TRANSLATION).displayName} voice data",
                message = "To change language, follow these steps: \n\n" +
                        "1. Click --> \"Proceed\" \n" +
                        "2. Click --> \"Preferred engine\" \n" +
                        "3. Select --> \"Speech Recognition and Synthesis from Google\" \n" +
                        "4. Press --> Back button \n" +
                        "5. Click --> \"Language\" \n" +
                        "6. Select -->  \"${Locale(Constants.CURRENT_TRANSLATION).displayName}\" \n\n" +
                        "To change voice output, continue these steps: \n\n" +
                        "7. Press --> Back button \n" +
                        "8. Click --> Settings icon \n" +
                        "9. Click --> \"Install voice data\" \n" +
                        "10. Select --> \"${Locale(Constants.CURRENT_TRANSLATION).displayName}\" \n" +
                        "11. Click --> \"Download\" \n" +
                        "12. Select --> Voice",
                positiveTxt = "Proceed",
                negativeTxt = "Skip",
                positiveListener = { _, _ ->
                    mainActivity.hideDialog()
                    Intent("com.android.settings.TTS_SETTINGS").apply {
                        startActivity(this)
                    }
                },
                negativeListener = { _, _ ->
                    mainActivity.hideDialog()
                    print()
                })
        }

        btPlayback.setOnClickListener {
            if (!mainActivity.checkInternetConnection())
            {
                btPlayback.isChecked = false
                return@setOnClickListener
            }
            if (!audioRunning)
            {
                mainActivity.showCustomDialog(R.layout.dialog_progress_audio)
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
    }

    private fun updateCurrentAyahIndex(): Boolean
    {
        val lastAyahIndex = surahs[surahIndex].ayahs.size - 1
        val lastSurahIndex = surahs.size - 1
        return if (ayahIndex < lastAyahIndex)
        {
            ayahIndex++
            true
        } else if (surahIndex < lastSurahIndex)
        {
            ayahIndex = 0
            surahIndex++
            true
        } else
        {
            false
        }
    }

    override fun onPrepared(mp1: MediaPlayer?)
    {
        mainActivity.hideDialog()
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

    override fun onResume()
    {
        super.onResume()
        tts.setLanguage(Locale(Constants.CURRENT_TRANSLATION))
        mainActivity.toolbar.inflateMenu(R.menu.menu3)
        mainActivity.toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.page -> {
//                    val transaction = mainActivity.supportFragmentManager.beginTransaction()
//                    transaction.replace(R.id.container, SettingsFragment.newInstance())
//                    transaction.addToBackStack(null)
//                    transaction.commit()

                    val inflater = LayoutInflater.from(mainActivity)
                    val view = inflater.inflate(R.layout.dialog_settings, null, false).apply {

                        var scriptFont = ""
                        var translation = ""
                        var translationFont = ""
                        var scriptText = Constants.SCRIPTS.values.toList()[0]
                        var translatedText = ""

                        val spScriptFont: Spinner = findViewById(R.id.spScriptFont)
                        val spTranslation: Spinner = findViewById(R.id.spTranslation)
                        val spTranslationFont: Spinner = findViewById(R.id.spTranslationFont)
                        val sbFontSize: SeekBar = findViewById(R.id.sbFontSize)
                        val wv: WebView = findViewById(R.id.wb)
                        val btSave: Button = findViewById(R.id.btSave)
                        val ibClose: ImageButton = findViewById(R.id.ibClose)

                        fun printEg() {
                            val style = "<style>\n" +
                                    "@import url('https://fonts.googleapis.com/css2?family=Arial&family=Scheherazade:wght@700&family=Lateef&family=Mirza&display=swap');\n" +
                                    "@import url('https://fonts.googleapis.com/css2?family=Roboto&family=Open+Sans&family=Poppins&family=Georgia&family=Times+New+Roman&display=swap');\n" +
                                    "@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+Devanagari&family=Lohit+Devanagari&family=Samarkan&family=Hind&family=Kalam&display=swap');\n" +
                                    "@font-face {\n" +
                                    "   font-family: 'indopak_font';\n" +
                                    "   src: url('file:///android_asset/indopak_font.woff');\n" +
                                    "}\n" +
                                    "*{\n" +
                                    "   text-align: justify;\n" +
                                    "   text-align-last: center;\n" +
                                    "}\n" +
                                    "body {\n" +
                                    "   display: flex;\n" +
                                    "   justify-content: center;\n" +
                                    "   align-items: center;\n" +
                                    "   min-height: 100vh;\n" +
                                    "   margin: 0;\n" +
                                    "   padding: 16px;\n" +
                                    "   flex-direction: column;\n" +
                                    "   text-align: center;\n" +
                                    "   background-color: #873ed511;\n" +
                                    "}\n" +
                                    ".ar {\n" +
                                    "   font-family: $scriptFont;\n" +
                                    "   padding-top: 5px;\n" +
                                    "   padding-bottom: 10px;\n" +
                                    "   font-size: 175%;\n" +
                                    "   direction: rtl;\n" +
                                    "}\n" +
                                    ".hi {\n" +
                                    "   font-family: $translationFont;\n" +
                                    "   font-size: 150%;\n" +
                                    "   padding-top: 5px;\n" +
                                    "   padding-bottom: 10px;\n" +
                                    "   direction: rtl;\n" +
                                    "}\n" +
                                    ".ur {\n" +
                                    "   font-family: $translationFont;\n" +
                                    "   font-size: 150%;\n" +
                                    "   padding-top: 5px;\n" +
                                    "   padding-bottom: 10px;\n" +
                                    "   direction: rtl;\n" +
                                    "}\n" +
                                    ".id {\n" +
                                    "   font-family: $translationFont;\n" +
                                    "   font-size: 100%;\n" +
                                    "   padding-top: 5px;\n" +
                                    "   padding-bottom: 10px;\n" +
                                    "   direction: ltr;\n" +
                                    "}\n" +
                                    ".en {\n" +
                                    "   font-family: $translationFont;\n" +
                                    "   font-size: 100%;\n" +
                                    "   padding-top: 5px;\n" +
                                    "   padding-bottom: 10px;\n" +
                                    "   direction: ltr;\n" +
                                    "}\n" +
                                    ".containerAyah {\n" +
                                    "   color: black;\n" +
                                    "   border: 1px solid black;\n" +
                                    "   border-radius: 50%;\n" +
                                    "   font-size: 0.5em;\n" +
                                    "   display: inline-flex;\n" +
                                    "   justify-content: center;\n" +
                                    "   align-items: center;\n" +
                                    "   padding: 0.5em;\n" +
                                    "   width: 1em;height: 1em;\n" +
                                    "}\n" +
                                    "</style>\n"

                            val body = "<body>\n" +
                                    "<div class='ar'>\n" +
                                    "<span>${scriptText}ِ<span class='containerAyah'>1</span></span>\n" +
                                    "</div>\n" +
                                    "<div class='$translation'>\n" +
                                    "<span>$translatedText</span>\n" +
                                    "</div>\n" +
                                    "</body>\n"

                            wv.loadDataWithBaseURL(
                                null,
                                "<html><head>$style</head>$body</html>",
                                "text/html",
                                "UTF-8", null
                            ).toString()
                        }

                        //region set spinner adapters
                        val scriptFontAdapter: ArrayAdapter<String> =
                            ArrayAdapter<String>(
                                activity!!,
                                android.R.layout.simple_spinner_item,
                                Constants.AR_FONTS.keys.toList()
                            )
                        scriptFontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spScriptFont.adapter = scriptFontAdapter

                        val translationAdapter: ArrayAdapter<String> =
                            ArrayAdapter<String>(
                                activity!!,
                                android.R.layout.simple_spinner_item,
                                Constants.TRANSLATIONS.keys.toList()
                            )
                        translationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spTranslation.adapter = translationAdapter

                        val translationFontAdapter: ArrayAdapter<String> =
                            ArrayAdapter<String>(
                                activity!!,
                                android.R.layout.simple_spinner_item,
                                Constants.EN_FONTS.keys.toList()
                            )
                        translationFontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spTranslationFont.adapter = translationFontAdapter
                        //endregion

                        //region set selected settings options
                        spScriptFont.setSelection(
                            Constants.AR_FONTS.values.toList().indexOf(Constants.CURRENT_SCRIPT_FONT)
                        )

                        spTranslation.setSelection(
                            Constants.TRANSLATIONS.keys.toList().indexOf(Constants.CURRENT_TRANSLATION)
                        )

                        when (Constants.CURRENT_TRANSLATION) {
                            "en" -> spTranslationFont.setSelection(
                                Constants.EN_FONTS.values.toList().indexOf(Constants.CURRENT_TRANSLATION_FONT)
                            )
                            "hi" -> spTranslationFont.setSelection(
                                Constants.HI_FONTS.values.toList().indexOf(Constants.CURRENT_TRANSLATION_FONT)
                            )
                            "id" -> spTranslationFont.setSelection(
                                Constants.ID_FONTS.values.toList().indexOf(Constants.CURRENT_TRANSLATION_FONT)
                            )
                            "ur" -> spTranslationFont.setSelection(
                                Constants.UR_FONTS.values.toList().indexOf(Constants.CURRENT_TRANSLATION_FONT)
                            )
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            delay(TimeUnit.SECONDS.toMillis(0.5.toLong()))
                            withContext(Dispatchers.Main) {
                                sbFontSize.progress = Constants.CURRENT_ZOOM
                            }
                        }
                        //endregion

                        //region click listeners
                        spScriptFont.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                scriptFont = Constants.AR_FONTS.values.toList()[position]
                                printEg()
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }

                        spTranslation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                translation = Constants.TRANSLATIONS.keys.toList()[position]
                                translatedText = Constants.TRANSLATIONS.values.toList()[position]
                                translationFontAdapter.clear()
                                when (translation) {
                                    "en" -> translationFontAdapter.addAll(Constants.EN_FONTS.keys.toList())
                                    "hi" -> translationFontAdapter.addAll(Constants.HI_FONTS.keys.toList())
                                    "id" -> translationFontAdapter.addAll(Constants.ID_FONTS.keys.toList())
                                    "ur" -> translationFontAdapter.addAll(Constants.UR_FONTS.keys.toList())
                                }
                                translationFontAdapter.notifyDataSetChanged()
                                printEg()
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }

                        spTranslationFont.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                when (translation) {
                                    "en" -> translationFont = Constants.EN_FONTS.values.toList()[position]
                                    "hi" -> translationFont = Constants.HI_FONTS.values.toList()[position]
                                    "id" -> translationFont = Constants.ID_FONTS.values.toList()[position]
                                    "ur" -> translationFont = Constants.UR_FONTS.values.toList()[position]
                                }
                                printEg()
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }

                        sbFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar, progress: Int,
                                fromUser: Boolean
                            ) {
                                wv.settings.textZoom = progress
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar) {
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar) {
                            }
                        })

                        btSave.setOnClickListener {
                            mainActivity.hideDialog()

                            val isSubscribed = sharedPreferences.getBoolean(Constants.KEY_IS_SUBSCRIBED, false)
                            if (isSubscribed) {
                                Constants.CURRENT_SCRIPT_FONT = scriptFont
                                Constants.CURRENT_TRANSLATION = translation
                                Constants.CURRENT_TRANSLATION_FONT = translationFont
                                Constants.CURRENT_ZOOM = sbFontSize.progress

                                this@QuranFragment.print()

                                val editor = sharedPreferences.edit()
                                editor.putString(Constants.KEY_SCRIPT_FONT, scriptFont)
                                editor.putString(Constants.KEY_TRANSLATION, translation)
                                editor.putString(Constants.KEY_TRANSLATION_FONT, translationFont)
                                editor.putInt(
                                    Constants.KEY_ZOOM, sbFontSize.progress
                                )
                                editor?.apply()
                            }
                        }

                        ibClose.setOnClickListener {
                            mainActivity.hideDialog()
                        }
                        //endregion
                    }
                    mainActivity.showCustomDialog(layout = view)
                }
                R.id.audio -> {
                    btPlayback.isChecked = false
                    audioRunning = false
                    mp?.release()
                    mp = null
                    tts.stop()
                    mainActivity.showCustomDialog(
                        title = "Translation Audio Settings",
                        message = "To change language, follow these steps: \n\n" +
                                "1. Click --> \"Proceed\" \n" +
                                "2. Click --> \"Preferred engine\" \n" +
                                "3. Select --> \"Speech Recognition and Synthesis from Google\" \n" +
                                "4. Press --> Back button \n" +
                                "5. Click --> \"Language\" \n" +
                                "6. Select -->  \"${Locale(Constants.CURRENT_TRANSLATION).displayName}\" \n\n" +
                                "To change voice output, continue these steps: \n\n" +
                                "7. Press --> Back button \n" +
                                "8. Click --> Settings icon \n" +
                                "9. Click --> \"Install voice data\" \n" +
                                "10. Select --> \"${Locale(Constants.CURRENT_TRANSLATION).displayName}\" \n" +
                                "11. Click --> \"Download\" \n" +
                                "12. Select --> Voice",
                        positiveTxt = "Proceed",
                        negativeTxt = "Later",
                        positiveListener = { _, _ ->
                            mainActivity.hideDialog()
                            Intent("com.android.settings.TTS_SETTINGS").apply {
                                startActivity(this)
                            }
                        },
                        negativeListener = { _, _ ->
                            mainActivity.hideDialog()
                        })
                }
            }
            false
        }
    }

    override fun onPause()
    {
        super.onPause()
        mainActivity.toolbar.menu.clear()
        btPlayback.isChecked = false
        audioRunning = false
        mp?.release()
        mp = null
        tts.stop()
    }

    override fun onStop()
    {
        super.onStop()
        mp?.release()
        mp = null
        tts.stop()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        tts.shutdown()
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
}