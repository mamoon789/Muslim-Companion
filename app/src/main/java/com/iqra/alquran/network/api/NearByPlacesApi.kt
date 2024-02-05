package com.iqra.alquran.network.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.iqra.alquran.network.models.NamazTimings
import com.iqra.alquran.network.models.NearByPlaces
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NearByPlacesApi {
    @GET("json?")
    suspend fun getNearByPlaces(
        @Query("location") location: String,
        @Query("radius") radius: String,
        @Query("type") type: String,
        @Query("key") key: String,
        ): Response<NearByPlaces>

    companion object {
        val BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
        operator fun invoke(): NearByPlacesApi {
            val api: NearByPlacesApi by lazy {
                Retrofit.Builder()
                    .client(OkHttpClient.Builder().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .baseUrl(BASE_URL)
                    .build()
                    .create(NearByPlacesApi::class.java)
            }
            return api
        }
    }
}