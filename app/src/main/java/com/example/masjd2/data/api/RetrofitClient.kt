package com.example.masjd2.data.api

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.aladhan.com/v1/"
    private const val CACHE_SIZE = 10L * 1024 * 1024 // 10 MB cache

    private var prayerApiService: PrayerApiService? = null

    fun getPrayerApiService(context: Context): PrayerApiService {
        if (prayerApiService == null) {
            val cacheDir = File(context.cacheDir, "okhttp_cache")
            val cache = Cache(cacheDir, CACHE_SIZE)

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Cache-Control", "max-stale=604800") // 7 days stale
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            prayerApiService = retrofit.create(PrayerApiService::class.java)
        }
        return prayerApiService!!
    }
}
