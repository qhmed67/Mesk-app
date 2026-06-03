package com.example.masjd2.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.masjd2.data.db.PrayerEntity
import com.example.masjd2.receivers.AthanBroadcastReceiver
import java.util.*

class AthanAlarmManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        private const val TAG = "AthanAlarmManager"
        
        const val FAJR_ALARM_ID = 1001
        const val DHUHR_ALARM_ID = 1002
        const val ASR_ALARM_ID = 1003
        const val MAGHRIB_ALARM_ID = 1004
        const val ISHA_ALARM_ID = 1005
        
        const val PRAYER_FAJR = "Fajr"
        const val PRAYER_DHUHR = "Dhuhr"
        const val PRAYER_ASR = "Asr"
        const val PRAYER_MAGHRIB = "Maghrib"
        const val PRAYER_ISHA = "Isha"
    }

    fun scheduleAllPrayerAlarms(prayerTimes: PrayerEntity, volume: Float = 1.0f, customPath: String? = null) {
        Log.d(TAG, "Scheduling all prayer alarms for ${prayerTimes.date}")

        val prayers = listOf(
            Triple(FAJR_ALARM_ID, PRAYER_FAJR, prayerTimes.fajrUtc),
            Triple(DHUHR_ALARM_ID, PRAYER_DHUHR, prayerTimes.dhuhrUtc),
            Triple(ASR_ALARM_ID, PRAYER_ASR, prayerTimes.asrUtc),
            Triple(MAGHRIB_ALARM_ID, PRAYER_MAGHRIB, prayerTimes.maghribUtc),
            Triple(ISHA_ALARM_ID, PRAYER_ISHA, prayerTimes.ishaUtc)
        )

        for ((alarmId, prayerName, utcMillis) in prayers) {
            if (utcMillis > System.currentTimeMillis()) {
                scheduleAlarm(utcMillis, prayerName, alarmId)
            }
        }

        AthanWorkScheduler.scheduleAllPrayerAlarms(context, prayerTimes, volume, customPath)

        Log.d(TAG, "Finished scheduling all prayer alarms")
    }

    private fun scheduleAlarm(utcMillis: Long, prayerName: String, alarmId: Int) {
        try {
            val intent = Intent(context, AthanBroadcastReceiver::class.java).apply {
                putExtra("prayer_name", prayerName)
                putExtra("alarm_id", alarmId)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val showIntent = PendingIntent.getActivity(
                        context, alarmId,
                        Intent(context, com.example.masjd2.MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(utcMillis, showIntent),
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled exact $prayerName alarm at ${Date(utcMillis)}")
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, utcMillis, pendingIntent)
                    Log.d(TAG, "Scheduled exact $prayerName alarm at ${Date(utcMillis)}")
                }
                else -> {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, utcMillis, pendingIntent)
                    Log.d(TAG, "Scheduled exact $prayerName alarm at ${Date(utcMillis)}")
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted for $prayerName - WorkManager fallback active")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling $prayerName alarm: ${e.message}", e)
        }
    }

    fun cancelAllPrayerAlarms() {
        val alarmIds = listOf(FAJR_ALARM_ID, DHUHR_ALARM_ID, ASR_ALARM_ID, MAGHRIB_ALARM_ID, ISHA_ALARM_ID)
        for (alarmId in alarmIds) {
            val intent = Intent(context, AthanBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        AthanWorkScheduler.cancelAllPrayerAlarms(context)
        Log.d(TAG, "Cancelled all prayer alarms")
    }

    fun cancelAlarm(alarmId: Int) {
        val intent = Intent(context, AthanBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleTestAlarm() {
        val testTime = System.currentTimeMillis() + 60_000
        val intent = Intent(context, AthanBroadcastReceiver::class.java).apply {
            putExtra("prayer_name", "Test")
            putExtra("alarm_id", 9999)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val showIntent = PendingIntent.getActivity(
                    context, 9999,
                    Intent(context, com.example.masjd2.MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(testTime, showIntent), pendingIntent)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, testTime, pendingIntent)
            }
            else -> {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, testTime, pendingIntent)
            }
        }
        Log.d(TAG, "Scheduled test alarm for ${Date(testTime)}")
    }
}
