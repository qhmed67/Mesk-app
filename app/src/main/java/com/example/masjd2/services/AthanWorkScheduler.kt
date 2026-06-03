package com.example.masjd2.services

import android.content.Context
import android.util.Log
import com.example.masjd2.data.db.PrayerEntity

object AthanWorkScheduler {

    private const val TAG = "AthanWorkScheduler"

    fun scheduleAllPrayerAlarms(
        context: Context,
        prayerTimes: PrayerEntity,
        volume: Float,
        customAthanPath: String?
    ) {
        val prayers = listOf(
            Triple("fajr", "Fajr", 1001 to prayerTimes.fajrUtc),
            Triple("dhuhr", "Dhuhr", 1002 to prayerTimes.dhuhrUtc),
            Triple("asr", "Asr", 1003 to prayerTimes.asrUtc),
            Triple("maghrib", "Maghrib", 1004 to prayerTimes.maghribUtc),
            Triple("isha", "Isha", 1005 to prayerTimes.ishaUtc)
        )

        for ((prayerKey, name, pair) in prayers) {
            val (alarmId, utcMillis) = pair
            if (utcMillis > 0L) {
                AthanScheduleWorker.enqueuePrayerWork(
                    context, prayerKey, name, alarmId, utcMillis, volume, customAthanPath
                )
            }
        }

        context.getSharedPreferences("athan_prefs", Context.MODE_PRIVATE).edit()
            .putFloat("athan_volume", volume)
            .putString("custom_athan_path", customAthanPath)
            .apply()

        Log.d(TAG, "Scheduled all prayer alarms via WorkManager")
    }

    fun cancelAllPrayerAlarms(context: Context) {
        AthanScheduleWorker.cancelAllPrayerWork(context)
        Log.d(TAG, "Cancelled all prayer alarms")
    }
}
