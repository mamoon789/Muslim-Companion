package com.iqra.alquran.network.models

import java.io.Serializable

data class Dhikr(val data: MutableList<Data>): Serializable{
    data class Data(val content: String, val description: String, val count: String, var progress: String = "0"): Serializable
}