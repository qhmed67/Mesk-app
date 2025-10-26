package com.example.masjd2.data.api

import com.google.gson.annotations.SerializedName

/**
 * API response models for AlAdhan API
 */

data class PrayerApiResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<PrayerDay>
)

data class PrayerDay(
    @SerializedName("timings")
    val timings: PrayerTimings,
    @SerializedName("date")
    val date: PrayerDate
)

data class PrayerTimings(
    @SerializedName("Fajr")
    val fajr: String,
    @SerializedName("Dhuhr")
    val dhuhr: String,
    @SerializedName("Asr")
    val asr: String,
    @SerializedName("Maghrib")
    val maghrib: String,
    @SerializedName("Isha")
    val isha: String
)

data class PrayerDate(
    @SerializedName("readable")
    val readable: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("gregorian")
    val gregorian: GregorianDate,
    @SerializedName("hijri")
    val hijri: HijriDate
)

data class GregorianDate(
    @SerializedName("date")
    val date: String,
    @SerializedName("format")
    val format: String,
    @SerializedName("day")
    val day: String,
    @SerializedName("weekday")
    val weekday: Weekday,
    @SerializedName("month")
    val month: Month,
    @SerializedName("year")
    val year: String
)

data class HijriDate(
    @SerializedName("date")
    val date: String,
    @SerializedName("format")
    val format: String,
    @SerializedName("day")
    val day: String,
    @SerializedName("weekday")
    val weekday: Weekday,
    @SerializedName("month")
    val month: Month,
    @SerializedName("year")
    val year: String
)

data class Weekday(
    @SerializedName("en")
    val en: String,
    @SerializedName("ar")
    val ar: String
)

data class Month(
    @SerializedName("number")
    val number: Int,
    @SerializedName("en")
    val en: String,
    @SerializedName("ar")
    val ar: String
)
