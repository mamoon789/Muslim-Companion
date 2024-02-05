package com.iqra.alquran.network.models

data class AsmaAlHusna(
    val code: Int,
    val status: String,
    val data: MutableList<Data>
) {
    data class Data(
        val name: String,
        val transliteration: String,
        val number: Int,
        val en: En
    ) {
        data class En(
            val meaning: String
        )
    }
}
