package com.example.masjd2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.masjd2.services.AthanService
import com.example.masjd2.services.AthanAlarmManager
import com.example.masjd2.services.DailyRescheduleWorker
import com.example.masjd2.services.PrayerNotificationService
import com.example.masjd2.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AthanBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AthanBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device rebooted - initializing autonomous alarm system")
                initializeAutonomousAlarmSystem(context)
            }
            else -> {
                val prayerName = intent.getStringExtra("prayer_name")
                val alarmId = intent.getIntExtra("alarm_id", -1)
                
                Log.d(TAG, "Prayer alarm triggered: $prayerName (ID: $alarmId)")
                
                if (prayerName != null) {
                    triggerAthanAlarm(context, prayerName, alarmId)
                }
            }
        }
    }

    private fun triggerAthanAlarm(context: Context, prayerName: String, alarmId: Int) {
        Log.d(TAG, "Triggering Athan for $prayerName")
        
        val prayerRepository = PrayerRepository(context)
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        
        coroutineScope.launch {
            try {
                val athanSettings = prayerRepository.getAthanSettings()
                
                athanSettings?.let { settings ->
                    if (settings.isAthanEnabled) {
                        val serviceIntent = Intent(context, AthanService::class.java).apply {
                            putExtra("prayer_name", prayerName)
                            putExtra("alarm_id", alarmId)
                            putExtra("athan_volume", settings.athanVolume)
                            putExtra("custom_athan_path", settings.customAthanPath)
                        }
                        ContextCompat.startForegroundService(context, serviceIntent)
                        Log.d(TAG, "Started AthanService for $prayerName")
                    } else {
                        Log.d(TAG, "Athan is disabled - skipping alarm for $prayerName")
                    }
                } ?: run {
                    Log.d(TAG, "No Athan settings found - skipping alarm for $prayerName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Athan settings: ${e.message}", e)
                val serviceIntent = Intent(context, AthanService::class.java).apply {
                    putExtra("prayer_name", prayerName)
                    putExtra("alarm_id", alarmId)
                    putExtra("athan_volume", 1.0f)
                    putExtra("custom_athan_path", null as String?)
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }

    private fun initializeAutonomousAlarmSystem(context: Context) {
        DailyRescheduleWorker.enqueueDailyReschedule(context)

        val prayerRepository = PrayerRepository(context)
        val alarmManager = AthanAlarmManager(context)
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        
        coroutineScope.launch {
            try {
                Log.d(TAG, "Starting autonomous alarm system initialization")
                prayerRepository.initializeAthanSettings()
                val athanSettings = prayerRepository.getAthanSettings()
                
                if (athanSettings?.isAthanEnabled == true) {
                    val hasPrayerTimes = prayerRepository.hasAnyPrayerTimes()
                    
                    if (hasPrayerTimes) {
                        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
                        
                        if (todayPrayerTimes != null) {
                            alarmManager.scheduleAllPrayerAlarms(
                                todayPrayerTimes,
                                athanSettings.athanVolume,
                                athanSettings.customAthanPath
                            )
                            Log.d(TAG, "Autonomous alarm system activated")
                            startPersistentNotificationService(context)
                        } else {
                            Log.w(TAG, "No prayer times found for today")
                        }
                    } else {
                        Log.w(TAG, "No prayer times in database")
                    }
                } else {
                    Log.d(TAG, "Athan is disabled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing autonomous alarm system: ${e.message}", e)
            }
        }
    }

    private fun startPersistentNotificationService(context: Context) {
        try {
            val serviceIntent = Intent(context, PrayerNotificationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
            Log.d(TAG, "Started persistent notification service")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting persistent notification service: ${e.message}", e)
        }
    }
}
