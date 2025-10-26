package com.example.masjd2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.masjd2.services.AthanService
import com.example.masjd2.services.AthanAlarmManager
import com.example.masjd2.services.PrayerNotificationService
import com.example.masjd2.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that handles alarm triggers and boot completion
 */
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
                // This is a prayer time alarm
                val prayerName = intent.getStringExtra("prayer_name")
                val alarmId = intent.getIntExtra("alarm_id", -1)
                
                Log.d(TAG, "Prayer alarm triggered: $prayerName (ID: $alarmId)")
                
                if (prayerName != null) {
                    triggerAthanAlarm(context, prayerName, alarmId)
                }
            }
        }
    }

    /**
     * Trigger the Athan alarm by starting the AthanService
     */
    private fun triggerAthanAlarm(context: Context, prayerName: String, alarmId: Int) {
        Log.d(TAG, "Triggering Athan for $prayerName")
        
        // Check if Athan is enabled in settings
        val prayerRepository = PrayerRepository(context)
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        
        coroutineScope.launch {
            try {
                val athanSettings = prayerRepository.getAthanSettings()
                
                athanSettings?.let { settings ->
                    if (settings.isAthanEnabled) {
                        // Start the Athan service
                        val serviceIntent = Intent(context, AthanService::class.java).apply {
                            putExtra("prayer_name", prayerName)
                            putExtra("alarm_id", alarmId)
                            putExtra("athan_volume", settings.athanVolume)
                            putExtra("custom_athan_path", settings.customAthanPath)
                        }
                        
                        // Start as foreground service
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
                // Start service anyway with default settings
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

    /**
     * Initialize the autonomous alarm system that works without user interaction
     */
    private fun initializeAutonomousAlarmSystem(context: Context) {
        val prayerRepository = PrayerRepository(context)
        val alarmManager = AthanAlarmManager(context)
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        
        coroutineScope.launch {
            try {
                Log.d(TAG, "Starting autonomous alarm system initialization")
                
                // Initialize Athan settings if they don't exist
                prayerRepository.initializeAthanSettings()
                
                // Check if Athan is enabled
                val athanSettings = prayerRepository.getAthanSettings()
                
                if (athanSettings?.isAthanEnabled == true) {
                    // Check if we have prayer times in the database
                    val hasPrayerTimes = prayerRepository.hasAnyPrayerTimes()
                    
                    if (hasPrayerTimes) {
                        // We have prayer times - schedule alarms for today
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                        val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
                        
                        if (todayPrayerTimes != null) {
                            alarmManager.scheduleAllPrayerAlarms(todayPrayerTimes)
                            Log.d(TAG, "✅ Autonomous alarm system activated - alarms scheduled for today")
                            
                            // Start persistent notification service
                            startPersistentNotificationService(context)
                        } else {
                            Log.w(TAG, "⚠️ No prayer times found for today - alarms not scheduled")
                        }
                    } else {
                        Log.w(TAG, "⚠️ No prayer times in database - autonomous system cannot activate")
                        // Note: User will need to open app once to download prayer times
                    }
                } else {
                    Log.d(TAG, "🔇 Athan is disabled - autonomous system not activated")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error initializing autonomous alarm system: ${e.message}", e)
            }
        }
    }

    /**
     * Start the persistent notification service for prayer time countdown
     */
    private fun startPersistentNotificationService(context: Context) {
        try {
            val serviceIntent = Intent(context, PrayerNotificationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
            Log.d(TAG, "Started persistent notification service")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting persistent notification service: ${e.message}", e)
        }
    }

    /**
     * Reschedule all alarms after device reboot (legacy method)
     */
    private fun rescheduleAlarms(context: Context) {
        val prayerRepository = PrayerRepository(context)
        val alarmManager = AthanAlarmManager(context)
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        
        coroutineScope.launch {
            try {
                // Check if Athan is enabled
                val athanSettings = prayerRepository.getAthanSettings()
                
                athanSettings?.let { settings ->
                    if (settings.isAthanEnabled) {
                        // Get today's prayer times and reschedule alarms
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                        val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
                        
                        if (todayPrayerTimes != null) {
                            alarmManager.scheduleAllPrayerAlarms(todayPrayerTimes)
                            Log.d(TAG, "Rescheduled all prayer alarms after reboot")
                        } else {
                            Log.w(TAG, "No prayer times found for today - cannot reschedule alarms")
                        }
                    } else {
                        Log.d(TAG, "Athan is disabled - not rescheduling alarms")
                    }
                } ?: run {
                    Log.d(TAG, "No Athan settings found - not rescheduling alarms")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms after reboot: ${e.message}", e)
            }
        }
    }
}
