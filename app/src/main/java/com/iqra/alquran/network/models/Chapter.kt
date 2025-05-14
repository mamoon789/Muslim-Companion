package com.iqra.alquran.network.models

data class Chapter(
    val chapters: List<Chapters>,
    val message: String,
    val status: Int
) {
    constructor(): this(emptyList(),"",201)
    data class Chapters(
        val bookSlug: String,
        val chapterArabic: String,
        val chapterEnglish: String,
        val chapterNumber: String,
        val chapterUrdu: String,
        val id: Int
    )
}