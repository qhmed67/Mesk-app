package com.example.masjd2.repository

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.masjd2.data.api.CalculationMethods
import com.example.masjd2.data.api.PrayerApiResponse
import com.example.masjd2.data.api.RetrofitClient
import com.example.masjd2.data.db.AthanSettingsEntity
import com.example.masjd2.data.db.PrayerDao
import com.example.masjd2.data.db.PrayerDatabase
import com.example.masjd2.data.db.PrayerEntity
import com.example.masjd2.data.db.UserPreferencesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for managing prayer times data
 * Handles API calls, local storage, and business logic
 */
class PrayerRepository(context: Context) {
    
    private val prayerDao: PrayerDao = PrayerDatabase.getDatabase(context).prayerDao()
    private val apiService = RetrofitClient.prayerApiService
    private val geocoder = Geocoder(context, Locale.getDefault())
    
    companion object {
        private const val TAG = "PrayerRepository"
    }
    
    /**
     * Get prayer times for today from local database
     */
    fun getTodayPrayerTimes(): Flow<PrayerEntity?> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return prayerDao.getPrayerTimesForDateFlow(today)
    }
    
    /**
     * Get user preferences
     */
    fun getUserPreferences(): Flow<UserPreferencesEntity?> {
        return prayerDao.getUserPreferencesFlow()
    }
    
    /**
     * Check if this is the first launch
     */
    suspend fun isFirstLaunch(): Boolean {
        val preferences = prayerDao.getUserPreferences()
        return preferences?.isFirstLaunch ?: true
    }
    
    /**
     * Get location info from coordinates
     */
    suspend fun getLocationInfo(latitude: Double, longitude: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val country = address.countryName ?: "Unknown"
                val city = address.locality ?: address.adminArea ?: "Unknown"
                Pair(country, city)
            } else {
                Pair("Unknown", "Unknown")
            }
        } catch (e: Exception) {
            Pair("Unknown", "Unknown")
        }
    }
    
    /**
     * Get calculation method based on country
     */
    fun getCalculationMethodForCountry(country: String): Int {
        return when (country.lowercase()) {
            "egypt" -> CalculationMethods.EGYPTIAN
            "saudi arabia" -> CalculationMethods.UMM_AL_QURA
            else -> CalculationMethods.MUSLIM_WORLD_LEAGUE
        }
    }
    
    /**
     * Get calculation method name
     */
    fun getCalculationMethodName(methodId: Int): String {
        return when (methodId) {
            CalculationMethods.EGYPTIAN -> "Egyptian"
            CalculationMethods.UMM_AL_QURA -> "Umm al-Qura"
            CalculationMethods.MUSLIM_WORLD_LEAGUE -> "Muslim World League"
            CalculationMethods.KARACHI -> "Karachi"
            else -> "Muslim World League"
        }
    }
    
    /**
     * Fetch and save prayer times for a full year from API
     */
    suspend fun fetchAndSavePrayerTimesForYear(
        latitude: Double,
        longitude: Double,
        year: Int,
        month: Int = 1 // Start from January
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val locationInfo = getLocationInfo(latitude, longitude)
            val country = locationInfo.first
            val city = locationInfo.second
            val calculationMethodId = getCalculationMethodForCountry(country)
            val calculationMethodName = getCalculationMethodName(calculationMethodId)

            // Clear existing data before fetching new year's data
            prayerDao.deleteAllPrayerTimes()

            val allMonthsData = mutableListOf<PrayerEntity>()
            for (m in month..12) {
                val response = apiService.getPrayerTimesForMonth(
                    latitude,
                    longitude,
                    calculationMethodId,
                    m,
                    year
                )

                if (response.isSuccessful && response.body() != null) {
                    val prayerEntities = convertApiResponseToEntities(
                        response.body()!!,
                        latitude,
                        longitude,
                        calculationMethodId
                    )
                    allMonthsData.addAll(prayerEntities)
                } else {
                    Log.e(TAG, "API Error for month $m: ${response.errorBody()?.string()}")
                    return@withContext false
                }
            }
            prayerDao.insertPrayerTimes(allMonthsData)

            // Save user preferences
            val userPrefs = UserPreferencesEntity(
                country = country,
                city = city,
                calculationMethod = calculationMethodName,
                latitude = latitude,
                longitude = longitude,
                isFirstLaunch = false
            )
            prayerDao.insertUserPreferences(userPrefs)
            Log.d(TAG, "Successfully fetched and saved prayer times for year $year")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching and saving prayer times: ${e.message}", e)
            false
        }
    }

    /**
     * Fetch prayer times for a full year from API
     */
    suspend fun fetchYearlyPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int
    ): Result<List<PrayerEntity>> = withContext(Dispatchers.IO) {
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val allPrayerTimes = mutableListOf<PrayerEntity>()
            
            // Fetch prayer times for each month of the current year
            for (month in 1..12) {
                val response = apiService.getPrayerTimesForMonth(latitude, longitude, method, month, currentYear)
                
                if (response.isSuccessful) {
                    val prayerData = response.body()
                    if (prayerData != null && prayerData.status == "OK") {
                        val monthPrayerTimes = convertApiResponseToEntities(prayerData, latitude, longitude, method)
                        allPrayerTimes.addAll(monthPrayerTimes)
                    }
                } else {
                    return@withContext Result.failure(Exception("API call failed: ${response.code()}"))
                }
            }
            
            // Save to local database
            prayerDao.insertPrayerTimes(allPrayerTimes)
            Log.d(TAG, "Saved ${allPrayerTimes.size} prayer times to database")
            
            // Verify the save worked
            val savedCount = prayerDao.getPrayerTimesCount()
            Log.d(TAG, "Database now contains $savedCount prayer times")
            
            Result.success(allPrayerTimes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save user preferences
     */
    suspend fun saveUserPreferences(
        country: String,
        city: String,
        calculationMethod: String,
        latitude: Double,
        longitude: Double,
        isFirstLaunch: Boolean = false
    ) {
        val preferences = UserPreferencesEntity(
            country = country,
            city = city,
            calculationMethod = calculationMethod,
            latitude = latitude,
            longitude = longitude,
            isFirstLaunch = isFirstLaunch,
            lastUpdated = System.currentTimeMillis()
        )
        prayerDao.insertUserPreferences(preferences)
    }
    
    /**
     * Mark first launch as completed
     */
    suspend fun markFirstLaunchCompleted() {
        prayerDao.updateFirstLaunchStatus(false)
    }
    
    /**
     * Check if prayer times are available for today
     */
    suspend fun hasPrayerTimesForToday(): Boolean {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val prayerTimes = prayerDao.getPrayerTimesForDate(today)
            val hasPrayerTimes = prayerTimes != null
            Log.d(TAG, "hasPrayerTimesForToday: $hasPrayerTimes, today: $today, prayerTimes: $prayerTimes")
            hasPrayerTimes
        } catch (e: Exception) {
            Log.e(TAG, "Error checking prayer times for today: ${e.message}", e)
            false
        }
    }
    
    /**
     * Check if database has any prayer times at all
     */
    suspend fun hasAnyPrayerTimes(): Boolean {
        return try {
            val count = prayerDao.getPrayerTimesCount()
            val earliestDate = prayerDao.getEarliestDate()
            val latestDate = prayerDao.getLatestDate()
            Log.d(TAG, "hasAnyPrayerTimes: count=$count, earliest=$earliestDate, latest=$latestDate")
            count > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if database has prayer times: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get prayer times count
     */
    suspend fun getPrayerTimesCount(): Int {
        return prayerDao.getPrayerTimesCount()
    }
    
    /**
     * Debug method to check database status
     */
    suspend fun debugDatabaseStatus(): String {
        return try {
            val count = prayerDao.getPrayerTimesCount()
            val earliestDate = prayerDao.getEarliestDate()
            val latestDate = prayerDao.getLatestDate()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val hasToday = prayerDao.getPrayerTimesForDate(today) != null
            
            "Database Status: count=$count, earliest=$earliestDate, latest=$latestDate, today=$today, hasToday=$hasToday"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting database status: ${e.message}", e)
            "Database Status: ERROR - ${e.message}"
        }
    }
    
    /**
     * Test database connection
     */
    suspend fun testDatabaseConnection(): Boolean {
        return try {
            val count = prayerDao.getPrayerTimesCount()
            Log.d(TAG, "Database connection test successful, count: $count")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Database connection test failed: ${e.message}", e)
            false
        }
    }
    
    /**
     * Clear all prayer times (for reload)
     */
    suspend fun clearAllPrayerTimes() {
        prayerDao.deleteAllPrayerTimes()
    }
    
    /**
     * Convert API response to PrayerEntity list
     */
    private suspend fun convertApiResponseToEntities(
        response: PrayerApiResponse,
        latitude: Double,
        longitude: Double,
        method: Int
    ): List<PrayerEntity> {
        val methodName = getCalculationMethodName(method)
        val locationInfo = getLocationInfo(latitude, longitude)
        
        return response.data.map { day ->
            PrayerEntity(
                date = formatDateFromApi(day.date.gregorian.date),
                fajr = formatTimeFromApi(day.timings.fajr),
                dhuhr = formatTimeFromApi(day.timings.dhuhr),
                asr = formatTimeFromApi(day.timings.asr),
                maghrib = formatTimeFromApi(day.timings.maghrib),
                isha = formatTimeFromApi(day.timings.isha),
                country = locationInfo.first,
                city = locationInfo.second,
                calculationMethod = methodName,
                latitude = latitude,
                longitude = longitude
            )
        }
    }
    
    /**
     * Format date from API response
     */
    private fun formatDateFromApi(apiDate: String): String {
        return try {
            // API returns date in format "DD-MM-YYYY", convert to "YYYY-MM-DD"
            val parts = apiDate.split("-")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
            } else {
                apiDate
            }
        } catch (e: Exception) {
            apiDate
        }
    }
    
    /**
     * Format time from API response and convert to 12-hour format
     */
    private fun formatTimeFromApi(apiTime: String): String {
        return try {
            // API returns time like "05:30 (EET)", extract just the time part
            val timePart = apiTime.split(" ")[0]
            convertTo12HourFormat(timePart)
        } catch (e: Exception) {
            convertTo12HourFormat(apiTime)
        }
    }
    
    /**
     * Convert 24-hour format to 12-hour AM/PM format
     */
    private fun convertTo12HourFormat(time24: String): String {
        return try {
            val parts = time24.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].toInt()
                val minute = parts[1]
                
                val (hour12, period) = when {
                    hour == 0 -> Pair(12, "AM")
                    hour < 12 -> Pair(hour, "AM")
                    hour == 12 -> Pair(12, "PM")
                    else -> Pair(hour - 12, "PM")
                }
                
                "$hour12:$minute $period"
            } else {
                time24
            }
        } catch (e: Exception) {
            time24
        }
    }

    // Athan Settings methods

    /**
     * Get Athan settings
     */
    suspend fun getAthanSettings(): AthanSettingsEntity? {
        return try {
            prayerDao.getAthanSettings()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Athan settings: ${e.message}", e)
            null
        }
    }

    /**
     * Get Athan settings as Flow
     */
    fun getAthanSettingsFlow(): Flow<AthanSettingsEntity?> {
        return prayerDao.getAthanSettingsFlow()
    }

    /**
     * Save Athan settings
     */
    suspend fun saveAthanSettings(settings: AthanSettingsEntity) {
        try {
            prayerDao.insertAthanSettings(settings)
            Log.d(TAG, "Athan settings saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving Athan settings: ${e.message}", e)
        }
    }

    /**
     * Update Athan enabled status
     */
    suspend fun updateAthanEnabled(enabled: Boolean) {
        try {
            prayerDao.updateAthanEnabled(enabled)
            Log.d(TAG, "Athan enabled status updated to: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating Athan enabled status: ${e.message}", e)
        }
    }

    /**
     * Update Athan volume
     */
    suspend fun updateAthanVolume(volume: Float) {
        try {
            prayerDao.updateAthanVolume(volume)
            Log.d(TAG, "Athan volume updated to: $volume")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating Athan volume: ${e.message}", e)
        }
    }

    /**
     * Update custom Athan path
     */
    suspend fun updateCustomAthanPath(path: String?) {
        try {
            prayerDao.updateCustomAthanPath(path)
            Log.d(TAG, "Custom Athan path updated to: $path")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating custom Athan path: ${e.message}", e)
        }
    }

    /**
     * Initialize default Athan settings if not exists
     */
    suspend fun initializeAthanSettings() {
        try {
            val existingSettings = prayerDao.getAthanSettings()
            if (existingSettings == null) {
                val defaultSettings = AthanSettingsEntity(
                    id = 1,
                    isAthanEnabled = true,
                    athanVolume = 1.0f,
                    customAthanPath = null
                )
                prayerDao.insertAthanSettings(defaultSettings)
                Log.d(TAG, "Default Athan settings initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Athan settings: ${e.message}", e)
        }
    }

    /**
     * Get prayer times for a specific date (suspend function for alarm scheduling)
     */
    suspend fun getPrayerTimesForDate(date: String): PrayerEntity? {
        return try {
            prayerDao.getPrayerTimesForDate(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting prayer times for date $date: ${e.message}", e)
            null
        }
    }
    
}
