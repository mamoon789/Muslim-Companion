package com.iqra.alquran.views

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.fragment.app.Fragment
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var spScript: Spinner
    private lateinit var spScriptFont: Spinner
    private lateinit var spTranslation: Spinner
    private lateinit var spTranslationFont: Spinner
    private lateinit var sbFontSize: SeekBar
    private lateinit var wv: WebView
    private lateinit var btSave: Button

    var script = ""
    var scriptFont = ""
    var translation = ""
    var translationFont = ""

    var scriptText = ""
    var translatedText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        spScript = view.findViewById(R.id.spScript)
        spScriptFont = view.findViewById(R.id.spScriptFont)
        spTranslation = view.findViewById(R.id.spTranslation)
        spTranslationFont = view.findViewById(R.id.spTranslationFont)
        sbFontSize = view.findViewById(R.id.sbFontSize)
        wv = view.findViewById(R.id.wb)
        btSave = view.findViewById(R.id.btSave)

        sharedPreferences =
            activity!!.getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val scriptsAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                activity!!.applicationContext,
                android.R.layout.simple_spinner_item,
                Constants.SCRIPTS.keys.toList()
            )
        scriptsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spScript.adapter = scriptsAdapter

        val scriptFontAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                activity!!.applicationContext,
                android.R.layout.simple_spinner_item,
                Constants.AR_FONTS.keys.toList()
            )
        scriptsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spScriptFont.adapter = scriptFontAdapter

        val translationAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                activity!!.applicationContext,
                android.R.layout.simple_spinner_item,
                Constants.TRANSLATIONS.keys.toList()
            )
        scriptsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTranslation.adapter = translationAdapter

        val translationFontAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                activity!!.applicationContext,
                android.R.layout.simple_spinner_item,
                Constants.EN_FONTS.keys.toList()
            )
        scriptsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTranslationFont.adapter = translationFontAdapter

        getSettings()

        spScript.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                script = Constants.SCRIPTS.keys.toList()[position]
                scriptText = Constants.SCRIPTS.values.toList()[position]
                print()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spScriptFont.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                scriptFont = Constants.AR_FONTS.values.toList()[position]
                print()
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
                    "ur" -> translationFontAdapter.addAll(Constants.UR_FONTS.keys.toList())
                }
                translationFontAdapter.notifyDataSetChanged()
//                spTranslationFont.setSelection(0)
                print()
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
                    "ur" -> translationFont = Constants.UR_FONTS.values.toList()[position]
                }
                print()
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
            val editor = sharedPreferences.edit()
            editor.putString(Constants.KEY_SCRIPT, script)
            editor.putString(Constants.KEY_SCRIPT_FONT, scriptFont)
            editor.putString(Constants.KEY_TRANSLATION, translation)
            editor.putString(Constants.KEY_TRANSLATION_FONT, translationFont)
            editor.putInt(
                Constants.KEY_ZOOM, wv.settings.textZoom
            )
            editor?.apply()
        }

        return view
    }

    private fun getSettings() {
        Constants.CURRENT_SCRIPT = sharedPreferences.getString(
            Constants.KEY_SCRIPT,
            Constants.SCRIPTS.keys.toList()[0]
        )!!
        spScript.setSelection(Constants.SCRIPTS.keys.toList().indexOf(Constants.CURRENT_SCRIPT))

        Constants.CURRENT_SCRIPT_FONT = sharedPreferences.getString(
            Constants.KEY_SCRIPT_FONT,
            Constants.AR_FONTS.values.toList()[0]
        )!!
        spScriptFont.setSelection(
            Constants.AR_FONTS.values.toList().indexOf(Constants.CURRENT_SCRIPT_FONT)
        )

        Constants.CURRENT_TRANSLATION = sharedPreferences.getString(
            Constants.KEY_TRANSLATION,
            Constants.TRANSLATIONS.keys.toList()[0]
        )!!
        spTranslation.setSelection(
            Constants.TRANSLATIONS.keys.toList().indexOf(Constants.CURRENT_TRANSLATION)
        )

        Constants.CURRENT_TRANSLATION_FONT = sharedPreferences.getString(
            Constants.KEY_TRANSLATION_FONT,
            Constants.EN_FONTS.values.toList()[0]
        )!!
        when (Constants.CURRENT_TRANSLATION) {
            "en" -> spTranslationFont.setSelection(
                Constants.EN_FONTS.values.toList().indexOf(Constants.CURRENT_TRANSLATION_FONT)
            )
            "ur" -> spTranslationFont.setSelection(
                Constants.UR_FONTS.values.toList().indexOf(Constants.CURRENT_TRANSLATION_FONT)
            )
        }

        Constants.CURRENT_ZOOM = sharedPreferences.getInt(
            Constants.KEY_ZOOM,
            100
        )
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.SECONDS.toMillis(0.5.toLong()))
            withContext(Dispatchers.Main) {
                sbFontSize.progress = Constants.CURRENT_ZOOM
            }
        }
    }

    fun print() {
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
                "}\n" +
                ".ar {\n" +
                "   font-family: $scriptFont;\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   font-size: 175%;\n" +
                "   direction: rtl;\n" +
                "}\n" +
                ".ur {\n" +
                "   font-family: $translationFont;\n" +
                "   font-size: 150%;\n" +
                "   padding-top: 5px;\n" +
                "   padding-bottom: 10px;\n" +
                "   direction: rtl;\n" +
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
                "   font-size: 0.7rem;\n" +
                "   display: inline-flex;\n" +
                "   justify-content: center;\n" +
                "   align-items: center;\n" +
                "   padding: 0.4em;\n" +
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

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}