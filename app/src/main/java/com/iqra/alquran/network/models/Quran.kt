package com.iqra.alquran.network.models

import java.io.Serializable

data class Quran(
    val code: Int,
    val `data`: Data,
    val status: String
) : Serializable {
    data class Data(
        val surahs: MutableList<Surah>,
        val edition: Edition
    ) : Serializable{
        data class Surah(
            val ayahs: MutableList<Ayah>,
            val englishName: String,
            val englishNameTranslation: String,
            val name: String,
            val number: Int,
            val revelationType: String
        ) : Serializable{
            data class Ayah(
                val hizbQuarter: Int,
                val juz: Int,
                val manzil: Int,
                val number: Int,
                val numberInSurah: Int,
                val page: Int,
                val ruku: Int,
                val sajda: Any,
                var text: String,
                var translation: String,
                var audio: String
            ): Serializable
        }

        data class Edition(
            val englishName: String,
            val format: String,
            val identifier: String,
            val language: String,
            val name: String,
            val type: String
        ): Serializable
    }
}