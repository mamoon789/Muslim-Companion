package com.iqra.alquran.network.models

data class ForexRates(
    val items: MutableList<Items>
) {
    data class Items(
        val xauPrice: String,
        val xagPrice: String
    )
}
