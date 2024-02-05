package com.iqra.alquran.repository

import com.iqra.alquran.network.api.ForexRatesApi
import com.iqra.alquran.network.api.NamazTimingsApi
import com.iqra.alquran.network.api.NearByPlacesApi
import com.iqra.alquran.network.models.*
import com.iqra.alquran.network.wrapper.Resource

class Repository : BaseRepository() {

    suspend fun getNamazTimings(
        timestamp: String,
        latitude: String,
        longitude: String
    ): Resource<NamazTimings> =
        safeApiCall { NamazTimingsApi().getNamazTimings(timestamp, latitude, longitude) }

    suspend fun getForexRates(currency: String): Resource<ForexRates> =
        safeApiCall { ForexRatesApi().getForexRates(currency) }

    suspend fun getNearByPlaces(
        location: String,
        radius: String,
        type: String,
        key: String
    ): Resource<NearByPlaces> =
        safeApiCall { NearByPlacesApi().getNearByPlaces(location, radius, type, key) }

    suspend fun getHijriTime(
        date: String
    ): Resource<HijriTime> = safeApiCall { NamazTimingsApi().getHijriTime(date) }

    suspend fun getAsmaAlHusna(): Resource<AsmaAlHusna> = safeApiCall { NamazTimingsApi().getAsmaAlHusna() }
}