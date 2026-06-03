package com.example.masjd2.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.masjd2.repository.PrayerRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class DailyRescheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DailyRescheduleWorker"
        private const val UNIQUE_WORK_NAME = "daily_prayer_reschedule"

        fun enqueueDailyReschedule(context: Context) {
            val dailyRequest = PeriodicWorkRequestBuilder<DailyRescheduleWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    dailyRequest
                )
            Log.d(TAG, "Enqueued daily prayer reschedule worker")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "DailyRescheduleWorker running - scheduling today's alarms")

        val repository = PrayerRepository(applicationContext)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayPrayerTimes = repository.getPrayerTimesForDate(today)

        if (todayPrayerTimes != null) {
            val settings = repository.getAthanSettings()
            if (settings?.isAthanEnabled == true) {
                AthanWorkScheduler.scheduleAllPrayerAlarms(
                    applicationContext,
                    todayPrayerTimes,
                    settings.athanVolume,
                    settings.customAthanPath
                )
                Log.d(TAG, "Today's prayer alarms scheduled via WorkManager")
            }
        }

        return Result.success()
    }
}
