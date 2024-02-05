package com.iqra.alquran.network.models

data class NamazTimings(
    val data: Data
) {
    data class Data(
        val timings: Timings
    ) {
        data class Timings(
            val Fajr: String,
            val Dhuhr: String,
            val Asr: String,
            val Maghrib: String,
            val Isha: String
        )
    }
}
