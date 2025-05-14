@file:Suppress("SpellCheckingInspection")

package com.iqra.alquran.utils

import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdSize
import com.google.gson.Gson
import com.iqra.alquran.BuildConfig
import com.iqra.alquran.network.models.Quran
import com.iqra.alquran.network.models.Quran.Data.Surah

class Utility
{
    companion object
    {
        fun getBannerAdSize(activity: FragmentActivity): AdSize
        {
            val displayMetrics = activity.resources.displayMetrics
            val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                windowMetrics.bounds.width()
            } else
            {
                displayMetrics.widthPixels
            }
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        }

        fun getQuran(activity: FragmentActivity): Quran
        {
            val inputStream = activity.assets.open("id/quran.json")
            val quranJson = inputStream.bufferedReader().use { it.readText() }
            return Gson().fromJson(quranJson, Quran::class.java)
        }

        private fun getSurahs(activity: FragmentActivity): MutableList<Surah>
        {
            var inputStream = activity.assets.open("id/translation.json")
            var quranJson = inputStream.bufferedReader().use { it.readText() }
            val surahsTranslation = Gson().fromJson(quranJson, Quran::class.java).data.surahs

            inputStream = activity.assets.open("audio.json")
            quranJson = inputStream.bufferedReader().use { it.readText() }
            val surahsAudio = Gson().fromJson(quranJson, Quran::class.java).data.surahs

            val surahs = getQuran(activity).data.surahs
            for (i in surahs.indices)
            {
                for (j in surahs[i].ayahs.indices)
                {
                    surahs[i].ayahs[j].audio = surahsAudio[i].ayahs[j].audio
                    surahs[i].ayahs[j].translation = surahsTranslation[i].ayahs[j].text
                }
            }
            return surahs
        }

        fun makeHtmlBodyWithTranslation(activity: FragmentActivity)
        {
            val surahs = getSurahs(activity)
            val juzs = mutableListOf<String>()
            var juz = 1
            var body = ""
            for (surah in surahs)
            {
                var ruku = surah.ayahs[0].ruku

                body += "<div class='ar'>\n"
                body += "<h2>${surah.name}</h2>\n"
                body += "</div>\n"

                for (item in surah.ayahs.indices)
                {
                    if (surah.ayahs[item].juz > juz)
                    {
                        juzs.add(body)
                        juz = surah.ayahs[item].juz
                        body = ""
                    }
                    val ayah = surah.ayahs[item]

                    when
                    {
                        ayah.numberInSurah == surah.ayahs.size ->
                        {
                            body += "<div class='ar'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyahAndTranslation(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span>\n"
                            body += "</div>\n"

                            body += "<div class='${BuildConfig.EDITION}'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}_tr'>${ayah.translation}</span>\n"
                            body += "</div>\n"
                            body += "<hr>\n"
                        }

                        ruku != surah.ayahs[item + 1].ruku ->
                        {
                            body += "<div class='ar'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyahAndTranslation(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span>\n"
                            body += "</div>\n"

                            body += "<div class='${BuildConfig.EDITION}'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}_tr'>${ayah.translation}</span>\n"
                            body += "</div>\n"
                            body += "<hr>\n"

                            ruku = surah.ayahs[item + 1].ruku
                        }

                        else ->
                        {
                            body += "<div class='ar'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyahAndTranslation(id)'>" +
                                    ayah.text +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "</span>\n"
                            body += "</div>\n"

                            body += "<div class='${BuildConfig.EDITION}'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}_tr'>${ayah.translation}</span>\n"
                            body += "</div>\n"
                        }
                    }
                }
            }
            juzs.add(body)
        }

        fun makeHtmlBodyWithoutTranslation(activity: FragmentActivity)
        {
            val surahs = getSurahs(activity)
            val juzs = mutableListOf<String>()
            var juz = 1
            var body = ""
            body += "<div class='ar'>\n"

            for (surah in surahs)
            {
                body += "<h2>${surah.name}</h2>\n"

                var ruku = surah.ayahs[0].ruku

                for (i in surah.ayahs.indices)
                {
                    if (surah.ayahs[i].juz > juz)
                    {
                        juzs.add(body)
                        juz = surah.ayahs[i].juz
                        body = ""
                    }

                    val ayah = surah.ayahs[i]

                    when
                    {
                        ayah.numberInSurah == surah.ayahs.size ->
                        {
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyah(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span><hr>\n"
                        }

                        ruku != surah.ayahs[i + 1].ruku ->
                        {
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyah(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span><hr>\n"
                            ruku = surah.ayahs[i + 1].ruku
                        }

                        else ->
                        {
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyah(id)'>" +
                                    ayah.text +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "</span>\n"
                        }
                    }
                }
            }
            body += "</div>\n"
            juzs.add(body)
        }

        fun getHtmlBody(
            activity: FragmentActivity?,
            juz: String,
            translationActive: Boolean
        ): String
        {
            if (activity == null) return ""
            val inputStream =
                if (translationActive) activity.assets.open("juz_translation/${juz}")
                else activity.assets.open("juz/${juz}")
            return inputStream.bufferedReader().use { it.readText() }
        }
    }
}