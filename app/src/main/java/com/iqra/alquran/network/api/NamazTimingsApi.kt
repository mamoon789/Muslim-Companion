package com.iqra.alquran.network.api

import com.iqra.alquran.network.models.AsmaAlHusna
import com.iqra.alquran.network.models.HijriTime
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.iqra.alquran.network.models.NamazTimings
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NamazTimingsApi {
    @GET("timings/{timestamp}")
    suspend fun getNamazTimings(
        @Path("timestamp") timestamp: String,
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String
    ): Response<NamazTimings>

    @GET("gToH")
    suspend fun getHijriTime(
        @Query("date") date: String,
        ): Response<HijriTime>

    @GET("asmaAlHusna")
    suspend fun getAsmaAlHusna(): Response<AsmaAlHusna>

    companion object {
        val BASE_URL = "http://api.aladhan.com/v1/"
        operator fun invoke(): NamazTimingsApi {
            val api: NamazTimingsApi by lazy {
                Retrofit.Builder()
                    .client(OkHttpClient.Builder().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .baseUrl(BASE_URL)
                    .build()
                    .create(NamazTimingsApi::class.java)
            }
            return api
        }
    }
}