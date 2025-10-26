package com.example.masjd2.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room entity for storing prayer times
 */
@Entity(tableName = "prayer_times")
data class PrayerEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val fajr: String, // Format: HH:mm
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
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
