package com.iqra.alquran.network.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.iqra.alquran.network.models.ForexRates
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ForexRatesApi {
    @GET("{currency}")
    suspend fun getForexRates(@Path("currency") currency: String): Response<ForexRates>

    companion object {
        val BASE_URL = "http://data-asg.goldprice.org/dbXRates/"
        operator fun invoke(): ForexRatesApi {
            val api: ForexRatesApi by lazy {
                Retrofit.Builder()
                    .client(OkHttpClient.Builder().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .baseUrl(BASE_URL)
                    .build()
                    .create(ForexRatesApi::class.java)
            }
            return api
        }
    }
}