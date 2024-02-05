package com.iqra.alquran.network.models

data class HijriTime(
    val code: Int,
    val status: String,
    val data: Data
) {
    data class Data(
        val hijri: Hijri,
        val gregorian: Gregorian
    ) {
        data class Hijri(
            val date: String,
            val format: String,
            val day: String,
            val month: Month,
            val year: String,
            val designation: Designation
        ) {
            data class Month(
                val number: Int,
                val en: String,
                val ar: String
            )

            data class Designation(
                val abbreviated: String,
                val expanded: String
            )
        }

        data class Gregorian(
            val date: String,
            val format: String,
            val day: String,
            val month: Month,
            val year: String,
            val designation: Designation
        ) {
            data class Month(
                val number: Int,
                val en: String,
                val ar: String
            )

            data class Designation(
                val abbreviated: String,
                val expanded: String
            )
        }
    }
}
