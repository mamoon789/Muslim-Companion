package com.iqra.alquran.network.models

import java.io.Serializable

data class Book(
    val books: List<Books>,
    val message: String,
    val status: Int
): Serializable {
    constructor():this(emptyList(),"",201)
    data class Books(
        val aboutWriter: String,
        val bookName: String,
        val bookSlug: String,
        val chapters_count: String,
        val hadiths_count: String,
        val id: Int,
        val writerDeath: String,
        val writerName: String
    ): Serializable
}