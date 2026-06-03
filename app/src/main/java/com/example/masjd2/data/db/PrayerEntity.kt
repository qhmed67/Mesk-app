package com.example.masjd2.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZoneId

/**
 * Room entity for storing prayer times
 * All prayer times stored as both display strings and UTC epoch millis
 * UTC timestamps are timezone-independent and immune to DST shifts
 */
@Entity(tableName = "prayer_times")
data class PrayerEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val fajr: String, // Format: h:mm AM/PM (12-hour) for display
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    // UTC epoch millis for each prayer (timezone-independent for alarm scheduling)
    val fajrUtc: Long = 0L,
    val dhuhrUtc: Long = 0L,
    val asrUtc: Long = 0L,
    val maghribUtc: Long = 0L,
    val ishaUtc: Long = 0L,
    // IANA timezone ID used when computing UTC values (e.g., "Africa/Cairo")
    val timezoneId: String = ZoneId.systemDefault().id,
    val country: String,
    val city: String,
    val calculationMethod: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entity for storing user preferences
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1, // Single row
    val country: String,
    val city: String,
    val calculationMethod: String,
    val latitude: Double,
    val longitude: Double,
    val isFirstLaunch: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Entity for storing Athan alarm settings
 */
@Entity(tableName = "athan_settings")
data class AthanSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Single row
    val isAthanEnabled: Boolean = true, // Enable/disable all Athan alarms
    val athanVolume: Float = 1.0f, // Volume level (0.0 to 1.0)
    val customAthanPath: String? = null // Path to custom MP3 file, null for default sound
)
