package com.example.masjd2.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for prayer times and user preferences
 */
@Dao
interface PrayerDao {
    
    // Prayer Times operations
    @Query("SELECT * FROM prayer_times WHERE date = :date")
    suspend fun getPrayerTimesForDate(date: String): PrayerEntity?
    
    @Query("SELECT * FROM prayer_times WHERE date = :date")
    fun getPrayerTimesForDateFlow(date: String): Flow<PrayerEntity?>
    
    @Query("SELECT * FROM prayer_times ORDER BY date ASC")
    suspend fun getAllPrayerTimes(): List<PrayerEntity>
    
    @Query("SELECT * FROM prayer_times WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getPrayerTimesForDateRange(startDate: String, endDate: String): List<PrayerEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayerTimes: List<PrayerEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerEntity)
    
    @Query("DELETE FROM prayer_times")
    suspend fun deleteAllPrayerTimes()
    
    @Query("DELETE FROM prayer_times WHERE date < :date")
    suspend fun deleteOldPrayerTimes(date: String)
    
    // User Preferences operations
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getUserPreferences(): UserPreferencesEntity?
    
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferencesFlow(): Flow<UserPreferencesEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferencesEntity)
    
    @Query("UPDATE user_preferences SET isFirstLaunch = :isFirstLaunch WHERE id = 1")
    suspend fun updateFirstLaunchStatus(isFirstLaunch: Boolean)
    
    @Query("UPDATE user_preferences SET lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateLastUpdated(timestamp: Long)
    
    // Utility queries
    @Query("SELECT COUNT(*) FROM prayer_times")
    suspend fun getPrayerTimesCount(): Int
    
    @Query("SELECT MIN(date) FROM prayer_times")
    suspend fun getEarliestDate(): String?
    
    @Query("SELECT MAX(date) FROM prayer_times")
    suspend fun getLatestDate(): String?

    // Athan Settings methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthanSettings(settings: AthanSettingsEntity)

    @Query("SELECT * FROM athan_settings WHERE id = 1 LIMIT 1")
    suspend fun getAthanSettings(): AthanSettingsEntity?

    @Query("SELECT * FROM athan_settings WHERE id = 1 LIMIT 1")
    fun getAthanSettingsFlow(): Flow<AthanSettingsEntity?>

    @Query("UPDATE athan_settings SET isAthanEnabled = :enabled WHERE id = 1")
    suspend fun updateAthanEnabled(enabled: Boolean)

    @Query("UPDATE athan_settings SET athanVolume = :volume WHERE id = 1")
    suspend fun updateAthanVolume(volume: Float)

    @Query("UPDATE athan_settings SET customAthanPath = :path WHERE id = 1")
    suspend fun updateCustomAthanPath(path: String?)
}
