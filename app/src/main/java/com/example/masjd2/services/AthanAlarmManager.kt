package com.example.masjd2.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.masjd2.data.db.PrayerEntity
import com.example.masjd2.receivers.AthanBroadcastReceiver
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages scheduling and canceling Athan alarms for prayer times
 */
class AthanAlarmManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        private const val TAG = "AthanAlarmManager"
        
        // Request codes for different prayers
        const val FAJR_ALARM_ID = 1001
        const val DHUHR_ALARM_ID = 1002
        const val ASR_ALARM_ID = 1003
        const val MAGHRIB_ALARM_ID = 1004
        const val ISHA_ALARM_ID = 1005
        
        // Prayer names
        const val PRAYER_FAJR = "Fajr"
        const val PRAYER_DHUHR = "Dhuhr"
        const val PRAYER_ASR = "Asr"
        const val PRAYER_MAGHRIB = "Maghrib"
        const val PRAYER_ISHA = "Isha"
    }

    /**
     * Schedule all prayer alarms for today's prayer times
     */
    fun scheduleAllPrayerAlarms(prayerTimes: PrayerEntity) {
        Log.d(TAG, "Scheduling all prayer alarms for ${prayerTimes.date}")
        Log.d(TAG, "Prayer times: Fajr=${prayerTimes.fajr}, Dhuhr=${prayerTimes.dhuhr}, Asr=${prayerTimes.asr}, Maghrib=${prayerTimes.maghrib}, Isha=${prayerTimes.isha}")
        
        // Check if we can schedule exact alarms
        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "Cannot schedule exact alarms - permission not granted")
            return
        }
        
        // Schedule each prayer alarm
        scheduleAlarm(prayerTimes.date, prayerTimes.fajr, PRAYER_FAJR, FAJR_ALARM_ID)
        scheduleAlarm(prayerTimes.date, prayerTimes.dhuhr, PRAYER_DHUHR, DHUHR_ALARM_ID)
        scheduleAlarm(prayerTimes.date, prayerTimes.asr, PRAYER_ASR, ASR_ALARM_ID)
        scheduleAlarm(prayerTimes.date, prayerTimes.maghrib, PRAYER_MAGHRIB, MAGHRIB_ALARM_ID)
        scheduleAlarm(prayerTimes.date, prayerTimes.isha, PRAYER_ISHA, ISHA_ALARM_ID)
        
        Log.d(TAG, "Finished scheduling all prayer alarms")
    }

    /**
     * Schedule a single prayer alarm
     */
    private fun scheduleAlarm(date: String, time: String, prayerName: String, alarmId: Int) {
        try {
            Log.d(TAG, "Scheduling alarm for $prayerName at $time on $date")
            val alarmTime = parseDateTime(date, time)
            val currentTime = System.currentTimeMillis()
            
            Log.d(TAG, "Parsed alarm time: ${Date(alarmTime)}, Current time: ${Date(currentTime)}")
            
            // Only schedule if the prayer time is in the future
            if (alarmTime > currentTime) {
                val intent = Intent(context, AthanBroadcastReceiver::class.java).apply {
                    putExtra("prayer_name", prayerName)
                    putExtra("alarm_id", alarmId)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Schedule exact alarm with highest priority
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        // Android 12+ - Use setAlarmClock for highest priority
                        val showIntent = PendingIntent.getActivity(
                            context,
                            alarmId,
                            android.content.Intent(context, com.example.masjd2.MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmTime, showIntent)
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                        Log.d(TAG, "Scheduled alarm using setAlarmClock (highest priority)")
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime,
                            pendingIntent
                        )
                        Log.d(TAG, "Scheduled alarm using setExactAndAllowWhileIdle")
                    }
                    else -> {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime,
                            pendingIntent
                        )
                        Log.d(TAG, "Scheduled alarm using setExact")
                    }
                }
                
                Log.d(TAG, "Scheduled $prayerName alarm for ${Date(alarmTime)}")
            } else {
                Log.d(TAG, "Skipping $prayerName alarm - time has passed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling $prayerName alarm: ${e.message}", e)
        }
    }

    /**
     * Cancel all prayer alarms
     */
    fun cancelAllPrayerAlarms() {
        Log.d(TAG, "Canceling all prayer alarms")
        
        val alarmIds = listOf(FAJR_ALARM_ID, DHUHR_ALARM_ID, ASR_ALARM_ID, MAGHRIB_ALARM_ID, ISHA_ALARM_ID)
        
        for (alarmId in alarmIds) {
            val intent = Intent(context, AthanBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Canceled alarm with ID: $alarmId")
        }
    }

    /**
     * Cancel a specific prayer alarm
     */
    fun cancelAlarm(alarmId: Int) {
        val intent = Intent(context, AthanBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Canceled alarm with ID: $alarmId")
    }

    /**
     * Parse date and time strings to milliseconds
     */
    private fun parseDateTime(date: String, time: String): Long {
        // Convert 12-hour format to 24-hour format if needed
        val time24 = convertTo24HourFormat(time)
        
        val dateTimeString = "$date $time24"
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        return format.parse(dateTimeString)?.time ?: 0L
    }

    /**
     * Convert 12-hour format (with AM/PM) to 24-hour format
     */
    private fun convertTo24HourFormat(time12: String): String {
        return try {
            if (time12.contains("AM") || time12.contains("PM")) {
                val format12 = SimpleDateFormat("h:mm a", Locale.US)
                val format24 = SimpleDateFormat("HH:mm", Locale.US)
                val date = format12.parse(time12)
                format24.format(date!!)
            } else {
                // Already in 24-hour format
                time12
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting time format: $time12", e)
            time12
        }
    }

    /**
     * Check if exact alarm permission is granted (Android 12+)
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedule a test alarm 1 minute from now for debugging
     */
    fun scheduleTestAlarm() {
        try {
            val testTime = System.currentTimeMillis() + (60 * 1000) // 1 minute from now
            
            val intent = Intent(context, AthanBroadcastReceiver::class.java).apply {
                putExtra("prayer_name", "Test")
                putExtra("alarm_id", 9999)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule exact alarm with highest priority
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // Android 12+ - Use setAlarmClock for highest priority
                    val showIntent = PendingIntent.getActivity(
                        context,
                        9999,
                        android.content.Intent(context, com.example.masjd2.MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(testTime, showIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    Log.d(TAG, "Scheduled test alarm using setAlarmClock (highest priority)")
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        testTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled test alarm using setExactAndAllowWhileIdle")
                }
                else -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        testTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled test alarm using setExact")
                }
            }
            
            Log.d(TAG, "Scheduled test alarm for ${Date(testTime)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling test alarm: ${e.message}", e)
        }
    }
}
