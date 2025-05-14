package com.iqra.alquran.network.models

data class Hadith(
    val hadiths: Hadiths,
    val message: String,
    val status: Int
){
    constructor():this(Hadiths(),"",201)
    data class Hadiths(
        val current_page: Int,
        val `data`: List<Data>,
        val first_page_url: String,
        val from: Int,
        val last_page: Int,
        val last_page_url: String,
        val links: List<Link>,
        val next_page_url: String,
        val path: String,
        val per_page: Int,
        val prev_page_url: Any,
        val to: Int,
        val total: Int
    ){
        constructor():this(0, emptyList(), "",0,0,"", emptyList(),"","",0,"",0,0)
        data class Data(
            val book: Book,
            val bookSlug: String,
            val chapter: Chapter,
            val chapterId: String,
            val englishNarrator: String,
            val hadithArabic: String,
            val hadithEnglish: String,
            val hadithNumber: String,
            val hadithUrdu: String,
            val headingArabic: String,
            val headingEnglish: String,
            val headingUrdu: String,
            val id: Int,
            val status: String,
            val urduNarrator: String,
            val volume: String
        ){
            data class Book(
                val aboutWriter: Any,
                val bookName: String,
                val bookSlug: String,
                val id: Int,
                val writerDeath: String,
                val writerName: String
            )
            data class Chapter(
                val bookSlug: String,
                val chapterArabic: String,
                val chapterEnglish: String,
                val chapterNumber: String,
                val chapterUrdu: String,
                val id: Int
            )
        }
        data class Link(
            val active: Boolean,
            val label: String,
            val url: String
        )
    }
}