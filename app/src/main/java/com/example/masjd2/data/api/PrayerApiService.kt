package com.example.masjd2.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for AlAdhan API
 * Base URL: https://api.aladhan.com/v1/
 */
interface PrayerApiService {
    
    /**
     * Get prayer times for a specific month and year
     * Endpoint: calendar?latitude={lat}&longitude={lon}&method={id}&month={m}&year={y}
     * 
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param method Calculation method ID (3=MWL, 4=Umm al-Qura, 5=Egyptian)
     * @param month Month (1-12)
     * @param year Year (e.g., 2024)
     */
    @GET("calendar")
    suspend fun getPrayerTimesForMonth(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<PrayerApiResponse>
    
    /**
     * Get prayer times for today
     * Endpoint: timings?latitude={lat}&longitude={lon}&method={id}
     * 
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param method Calculation method ID
     */
    @GET("timings")
    suspend fun getPrayerTimesForToday(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int
    ): Response<PrayerApiResponse>
}

/**
 * Calculation method constants for AlAdhan API
 */
object CalculationMethods {
    const val MUSLIM_WORLD_LEAGUE = 3
    const val UMM_AL_QURA = 4
    const val EGYPTIAN = 5
    const val KARACHI = 1
    const val TEHRAN = 7
    const val SHIA_ITHNA_ASHARI = 0
}
