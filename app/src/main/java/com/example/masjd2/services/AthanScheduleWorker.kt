package com.example.masjd2.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.masjd2.data.db.PrayerEntity
import java.util.concurrent.TimeUnit

class AthanScheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "AthanScheduleWorker"

        const val KEY_PRAYER_NAME = "prayer_name"
        const val KEY_ALARM_ID = "alarm_id"
        const val KEY_UTC_MILLIS = "utc_millis"

        private const val UNIQUE_WORK_PREFIX = "athan_prayer_"

        fun enqueuePrayerWork(
            context: Context,
            prayerKey: String,
            prayerName: String,
            alarmId: Int,
            utcMillis: Long,
            volume: Float,
            customAthanPath: String?
        ) {
            val now = System.currentTimeMillis()
            val delay = utcMillis - now
            if (delay <= 0) {
                Log.d(TAG, "Skipping $prayerName - time has passed")
                return
            }

            val inputData = workDataOf(
                KEY_PRAYER_NAME to prayerName,
                KEY_ALARM_ID to alarmId
            )

            val workRequest = OneTimeWorkRequestBuilder<AthanScheduleWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("${UNIQUE_WORK_PREFIX}$prayerKey")
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${UNIQUE_WORK_PREFIX}$prayerKey",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            Log.d(TAG, "Scheduled WorkManager worker for $prayerName in ${delay / 1000}s")
        }

        fun cancelPrayerWork(context: Context, prayerKey: String) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("${UNIQUE_WORK_PREFIX}$prayerKey")
            Log.d(TAG, "Cancelled WorkManager work for $prayerKey")
        }

        fun cancelAllPrayerWork(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(UNIQUE_WORK_PREFIX)
        }
    }

    override suspend fun doWork(): Result {
        val prayerName = inputData.getString(KEY_PRAYER_NAME) ?: return Result.failure()
        val alarmId = inputData.getInt(KEY_ALARM_ID, -1)

        Log.d(TAG, "Worker triggered for $prayerName (alarmId=$alarmId)")

        val prefs = applicationContext.getSharedPreferences("athan_prefs", Context.MODE_PRIVATE)
        val volume = prefs.getFloat("athan_volume", 1.0f)
        val customPath = prefs.getString("custom_athan_path", null)

        val intent = android.content.Intent(applicationContext, AthanService::class.java).apply {
            putExtra("prayer_name", prayerName)
            putExtra("alarm_id", alarmId)
            putExtra("athan_volume", volume)
            putExtra("custom_athan_path", customPath)
        }
        androidx.core.content.ContextCompat.startForegroundService(applicationContext, intent)

        return Result.success()
    }
}
