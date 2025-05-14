package com.iqra.alquran.network.api

import com.iqra.alquran.network.models.Book
import com.iqra.alquran.network.models.Chapter
import com.iqra.alquran.network.models.Hadith
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HadithApi
{
    @GET("books")
    suspend fun getBooks(
        @Query("apiKey") apiKey: String = "\$2y\$10\$K7jeeeGT4kdYhNbUPVeSUuUo3vUf74iHeSRWUbATxKq2xblCCLAi"
    ): Response<Book>

    @GET("{bookSlug}/chapters")
    suspend fun getChapters(
        @Path("bookSlug") bookSlug: String,
        @Query("apiKey") apiKey: String = "\$2y\$10\$K7jeeeGT4kdYhNbUPVeSUuUo3vUf74iHeSRWUbATxKq2xblCCLAi"
    ): Response<Chapter>

    @GET("hadiths")
    suspend fun getHadiths(
        @Query("hadithEnglish") hadithEnglish: String,
        @Query("book") book: String,
        @Query("chapter") chapter: String,
        @Query("status") status: String,
        @Query("page") page: Int = 1,
        @Query("paginate") paginate: Int = 1000,
        @Query("apiKey") apiKey: String = "\$2y\$10\$K7jeeeGT4kdYhNbUPVeSUuUo3vUf74iHeSRWUbATxKq2xblCCLAi"
    ): Response<Hadith>

    companion object
    {
        private const val BASE_URL = "https://hadithapi.com/api/"
        operator fun invoke(): HadithApi
        {
            val api: HadithApi by lazy {
                Retrofit.Builder()
                    .client(OkHttpClient.Builder().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .baseUrl(BASE_URL)
                    .build()
                    .create(HadithApi::class.java)
            }
            return api
        }
    }

}