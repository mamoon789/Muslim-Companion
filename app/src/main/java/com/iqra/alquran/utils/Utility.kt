package com.iqra.alquran.utils

import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.iqra.alquran.network.models.Quran
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import com.iqra.alquran.network.models.Quran.Data.Surah

class Utility {
    companion object {
        //region ADD VERSE AUDIO & TRANSLATION
        fun addVerse(
            activity: FragmentActivity?,
            surahs: MutableList<Surah>,
            translationFile: String
        ): MutableList<Surah> {
            val quranJson = StringBuilder()
            var inputStream = activity?.assets?.open(translationFile)!!
            var bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                quranJson.append(line)
                quranJson.append('\n')
            }
            inputStream.close()
            bufferedReader.close()
            val quranTranslation = Gson().fromJson(quranJson.toString(), Quran::class.java)

            quranJson.clear()
            inputStream = activity.assets?.open("audio.json")!!
            bufferedReader = BufferedReader(InputStreamReader(inputStream))
            line = ""
            while (bufferedReader.readLine().also { line = it } != null) {
                quranJson.append(line)
                quranJson.append('\n')
            }
            inputStream.close()
            bufferedReader.close()
            val quranAudio = Gson().fromJson(quranJson.toString(), Quran::class.java)

            for (i in surahs.indices) {
                for (j in surahs[i].ayahs.indices) {
                    surahs[i].ayahs[j].audio = quranAudio.data.surahs[i].ayahs[j].audio
                    surahs[i].ayahs[j].translation = quranTranslation.data.surahs[i].ayahs[j].text
                }
            }
            return surahs
        }
        //endregion

        fun makeHtmlBodyWithTranslation(
            _body: String,
            surahs: MutableList<Surah>,
            edition: String
        ): String {
            val juzArray: MutableList<String> = mutableListOf()
            var juz = 1
            var body = _body
            for (surah in surahs) {
                var ruku = surah.ayahs[0].ruku

                body += "<div class='ar'>\n"
                body += "<h2>${surah.name}</h2>\n"
                body += "</div>\n"

                for (item in surah.ayahs.indices) {
                    if (surah.ayahs[item].juz > juz) {
                        juzArray.add(body)
                        juz = surah.ayahs[item].juz
                        body = _body
                    }
                    val ayah = surah.ayahs[item]

                    when {
                        ayah.numberInSurah == surah.ayahs.size -> {
                            body += "<div class='ar'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyahAndTranslation(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span>\n"
                            body += "</div>\n"

                            body += "<div class='$edition'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}_tr'>${ayah.translation}</span>\n"
                            body += "</div>\n"
                            body += "<hr>\n"
                        }
                        ruku != surah.ayahs[item + 1].ruku -> {
                            body += "<div class='ar'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyahAndTranslation(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span>\n"
                            body += "</div>\n"

                            body += "<div class='$edition'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}_tr'>${ayah.translation}</span>\n"
                            body += "</div>\n"
                            body += "<hr>\n"

                            ruku = surah.ayahs[item + 1].ruku
                        }
                        else -> {
                            body += "<div class='ar'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyahAndTranslation(id)'>" +
                                    ayah.text +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "</span>\n"
                            body += "</div>\n"

                            body += "<div class='$edition'>\n"
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}_tr'>${ayah.translation}</span>\n"
                            body += "</div>\n"
                        }
                    }
                }
            }
            juzArray.add(body)
            return body
        }

        fun makeHtmlBodyWithoutTranslation(_body: String, surahs: MutableList<Surah>): String {

            val juzArray: MutableList<String> = mutableListOf()
            var juz = 1
            var body = _body
            body += "<div class='ar'>\n"

            for (surah in surahs) {
                body += "<h2>${surah.name}</h2>\n"

                var ruku = surah.ayahs[0].ruku

                for (i in surah.ayahs.indices) {
                    if (surah.ayahs[i].juz > juz) {
                        juzArray.add(body)
                        juz = surah.ayahs[i].juz
                        body = _body
                    }

                    val ayah = surah.ayahs[i]

                    when {
                        ayah.numberInSurah == surah.ayahs.size -> {
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyah(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span><hr>\n"
                        }
                        ruku != surah.ayahs[i + 1].ruku -> {
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyah(id)'>" +
                                    ayah.text +
                                    "<div class='containerRuku'>" +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "<div class='ruku'>ع</div>" +
                                    "</div>" +
                                    "</span><hr>\n"
                            ruku = surah.ayahs[i + 1].ruku
                        }
                        else -> {
                            body += "<span id='surah${surah.number - 1}_ayah${ayah.numberInSurah - 1}' onClick='scrollToAyah(id)'>" +
                                    ayah.text +
                                    "<span class='containerAyah'>${ayah.numberInSurah}</span>" +
                                    "</span>\n"
                        }
                    }
                }
            }
            body += "</div>\n"
            juzArray.add(body)
            return body
        }

        fun getHtmlBody(
            activity: FragmentActivity?,
            juz: String,
            translationActive: Boolean
        ): String {
//            val surahs = StringBuilder()
            val inputStream: InputStream =
                if (translationActive) activity?.assets?.open("juz_en/${juz}")!!
                else activity?.assets?.open("juz/${juz}")!!
//            if (translationActive) {
//                inputStream =
//                    activity?.assets?.open(Constants.CURRENT_SCRIPT + "_" + Constants.CURRENT_TRANSLATION + ".html")!!
//            } else {
//                inputStream =
//                    activity?.assets?.open(Constants.CURRENT_SCRIPT + ".html")!!
//            }
//            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//            var line: String?
//            while (bufferedReader.readLine().also { line = it } != null) {
//                surahs.append(line)
//                surahs.append('\n')
//            }
//            inputStream.close()
//            bufferedReader.close()
//            return surahs.toString()
            return inputStream.bufferedReader().use { it.readText() }
        }

        //region REPLACE EXISTING VERSE OF UTHMANI SCRIPT WITH THE VERSE OF INDOPAK SCRIPT
        fun replaceVerse(
            activity: FragmentActivity?,
            surahs: MutableList<Surah>
        ): MutableList<Surah> {
            for (i in 1..114) {
                val fileName = "$i.json"
                val inputStream = activity?.assets?.open(fileName)!!
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                var index = 0
                while (bufferedReader.readLine().also { line = it } != null) {
                    if (line!!.length > 13) {
                        line = line?.substringAfterLast(": ")?.substringBeforeLast('"')
                            ?.substringAfterLast('"')

                        line = line?.replace("\\p{C}".toRegex(), "")
                        line = line?.replace("\\s+".toRegex(), " ")

                        surahs[i - 1].ayahs[index].text = line!!
                        index++
                    }
                }
                inputStream.close()
                bufferedReader.close()
            }
            return surahs
        }
        //endregion
    }
}