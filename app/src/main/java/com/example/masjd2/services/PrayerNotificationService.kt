package com.example.masjd2.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.masjd2.MainActivity
import com.example.masjd2.R
import com.example.masjd2.data.db.PrayerEntity
import com.example.masjd2.repository.PrayerRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Persistent foreground service that shows prayer time countdown in notification
 */
class PrayerNotificationService : Service() {

    companion object {
        private const val TAG = "PrayerNotificationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "prayer_countdown_channel"
        private const val CHANNEL_NAME = "Prayer Time Countdown"
        
        // Prayer names for display
        private val PRAYER_NAMES = mapOf(
            "fajr" to "Fajr",
            "dhuhr" to "Dhuhr", 
            "asr" to "Asr",
            "maghrib" to "Maghrib",
            "isha" to "Isha"
        )
    }

    private lateinit var prayerRepository: PrayerRepository
    private var serviceJob: Job? = null
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PrayerNotificationService created")
        
        prayerRepository = PrayerRepository(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "PrayerNotificationService started")
        
        // Start the notification immediately
        startForeground(NOTIFICATION_ID, createInitialNotification())
        
        // Start the countdown update loop
        startCountdownUpdates()
        
        // Return START_STICKY to restart service if killed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PrayerNotificationService destroyed")
        serviceJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Create notification channel for persistent prayer countdown
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance for persistent notification
            ).apply {
                description = "Shows countdown to next prayer time"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Create initial notification while loading prayer data
     */
    private fun createInitialNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🕌 Prayer Times")
        .setContentText("Loading prayer schedule...")
        .setSmallIcon(R.drawable.moon)
        .setOngoing(true)
        .setAutoCancel(false)
        .setContentIntent(createMainActivityIntent())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .build()

    /**
     * Create PendingIntent to open main activity when notification is tapped
     */
    private fun createMainActivityIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Start the coroutine that updates the countdown every minute
     */
    private fun startCountdownUpdates() {
        serviceJob?.cancel() // Cancel any existing job
        
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    updateNotificationWithPrayerInfo()
                    delay(60_000) // Update every minute
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating prayer notification: ${e.message}", e)
                    delay(60_000) // Wait before retrying
                }
            }
        }
    }

    /**
     * Update notification with current prayer information and countdown
     */
    private suspend fun updateNotificationWithPrayerInfo() {
        try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
            
            if (todayPrayerTimes != null) {
                val prayerInfo = getNextPrayerInfo(todayPrayerTimes)
                val notification = createPrayerNotification(prayerInfo)
                
                // Update notification on main thread
                withContext(Dispatchers.Main) {
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                }
                
                Log.d(TAG, "Updated notification: ${prayerInfo.title}")
            } else {
                // No prayer times available
                val notification = createNoPrayerTimesNotification()
                withContext(Dispatchers.Main) {
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                }
                Log.w(TAG, "No prayer times available for today")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateNotificationWithPrayerInfo: ${e.message}", e)
        }
    }

    /**
     * Get information about the next prayer time
     */
    private suspend fun getNextPrayerInfo(prayerTimes: PrayerEntity): PrayerInfo {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute
        
        // List of prayers in order with their times
        val prayers = listOf(
            "fajr" to prayerTimes.fajr,
            "dhuhr" to prayerTimes.dhuhr,
            "asr" to prayerTimes.asr,
            "maghrib" to prayerTimes.maghrib,
            "isha" to prayerTimes.isha
        )
        
        Log.d(TAG, "Current time: $currentHour:$currentMinute ($currentTotalMinutes minutes)")
        
        // Check each prayer in order to find the next one
        for ((prayerKey, prayerTime) in prayers) {
            val (prayerHour, prayerMinute) = parseTimeString(prayerTime)
            val prayerTotalMinutes = prayerHour * 60 + prayerMinute
            
            Log.d(TAG, "Checking $prayerKey: $prayerTime ($prayerHour:$prayerMinute = $prayerTotalMinutes minutes)")
            
            // If this prayer hasn't passed yet, it's the next one
            if (prayerTotalMinutes > currentTotalMinutes) {
                val minutesUntil = prayerTotalMinutes - currentTotalMinutes
                
                Log.d(TAG, "✓ Next prayer: $prayerKey today at ${formatTime12Hour(prayerTime)}")
                
                return PrayerInfo(
                    name = PRAYER_NAMES[prayerKey] ?: prayerKey,
                    time = formatTime12Hour(prayerTime),
                    countdown = formatCountdown(minutesUntil * 60 * 1000L),
                    title = "Next: ${PRAYER_NAMES[prayerKey]} today at ${formatTime12Hour(prayerTime)}"
                )
            } else {
                Log.d(TAG, "✗ $prayerKey has already passed")
            }
        }
        
        // All prayers have passed today, get tomorrow's Fajr
        Log.d(TAG, "All prayers passed today, getting tomorrow's Fajr")
        return getTomorrowFajrInfo()
    }
    
    /**
     * Parse time string "HH:mm AM/PM" or "h:mm AM/PM" to (hour, minute) pair in 24-hour format
     * For internal comparison only - display remains in 12-hour format
     */
    private fun parseTimeString(timeString: String): Pair<Int, Int> {
        return try {
            // Remove AM/PM and split by space
            val upper = timeString.uppercase().trim()
            val isAM = upper.contains("AM")
            val isPM = upper.contains("PM")
            
            // Extract the time part (before AM/PM)
            val timePart = when {
                isAM -> upper.replace("AM", "").trim()
                isPM -> upper.replace("PM", "").trim()
                else -> upper
            }
            
            val parts = timePart.split(":")
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()
            
            // Convert to 24-hour format for comparison (display stays 12-hour)
            if (isPM && hour != 12) {
                hour += 12
            } else if (isAM && hour == 12) {
                hour = 0
            }
            
            hour to minute
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time string: $timeString", e)
            0 to 0
        }
    }

    /**
     * Parse prayer time string to today's timestamp (or tomorrow if the time has passed today)
     */
    private fun parseTimeToday(timeString: String): Long {
        return try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            val time = timeFormat.parse(timeString)
            
            val calendar = Calendar.getInstance()
            val timeCalendar = Calendar.getInstance().apply {
                timeInMillis = time?.time ?: 0
            }
            
            // Set to today's date and the prayer time
            val now = Calendar.getInstance()
            
            calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // If the prayer time has already passed today, add one day to get the NEXT occurrence
            val nowTime = now.timeInMillis
            val prayerTime = calendar.timeInMillis
            
            // If prayer time is in the past today, it's the next day
            if (prayerTime <= nowTime) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeString", e)
            0L
        }
    }

    /**
     * Format time to 12-hour format with AM/PM
     */
    private fun formatTime12Hour(timeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
            val outputFormat = SimpleDateFormat("h:mm a", Locale.US)
            val time = inputFormat.parse(timeString)
            outputFormat.format(time ?: Date())
        } catch (e: Exception) {
            timeString // Return original if parsing fails
        }
    }

    /**
     * Format countdown duration to human-readable string
     */
    private fun formatCountdown(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Soon"
        }
    }

    /**
     * Get the end of today (midnight)
     */
    private fun getEndOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Get tomorrow's Fajr prayer information
     */
    private suspend fun getTomorrowFajrInfo(): PrayerInfo {
        return try {
            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            
            // Check if it's after midnight (12:00 AM) to determine "today" vs "tomorrow"
            val isAfterMidnight = currentHour < 6 // Between midnight and around 6 AM, Fajr is "today"
            val dayText = if (isAfterMidnight) "today" else "tomorrow"
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val tomorrow = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
            
            val tomorrowPrayerTimes = prayerRepository.getPrayerTimesForDate(tomorrow)
            
            if (tomorrowPrayerTimes != null) {
                val fajrTime = tomorrowPrayerTimes.fajr
                val (fajrHour, fajrMinute) = parseTimeString(fajrTime)
                
                // Calculate time until Fajr
                val nowTotalMinutes = currentHour * 60 + now.get(Calendar.MINUTE)
                
                // If it's after midnight (before sunrise), Fajr is "today" - calculate normally
                if (isAfterMidnight) {
                    val fajrTotalMinutes = fajrHour * 60 + fajrMinute
                    val minutesUntil = fajrTotalMinutes - nowTotalMinutes
                    
                    return PrayerInfo(
                        name = "Fajr",
                        time = formatTime12Hour(fajrTime),
                        countdown = formatCountdown(minutesUntil * 60 * 1000L),
                        title = "Next: Fajr today at ${formatTime12Hour(fajrTime)}"
                    )
                } else {
                    // It's after Isha but before midnight, so Fajr is "tomorrow"
                    val tomorrowFajrDateTime = parseTimeTomorrow(fajrTime)
                    val timeUntil = tomorrowFajrDateTime - now.timeInMillis
                    
                    return PrayerInfo(
                        name = "Fajr",
                        time = formatTime12Hour(fajrTime),
                        countdown = formatCountdown(timeUntil),
                        title = "Next: Fajr tomorrow at ${formatTime12Hour(fajrTime)}"
                    )
                }
            } else {
                // Fallback if tomorrow's data is not available
                PrayerInfo(
                    name = "Fajr",
                    time = "Unknown",
                    countdown = "Data unavailable",
                    title = "Next: Fajr tomorrow (time unavailable)"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tomorrow's Fajr info: ${e.message}", e)
            PrayerInfo(
                name = "Fajr",
                time = "Unknown",
                countdown = "Error",
                title = "Next: Fajr tomorrow (error loading time)"
            )
        }
    }

    /**
     * Parse prayer time string to tomorrow's timestamp
     */
    private fun parseTimeTomorrow(timeString: String): Long {
        return try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            val time = timeFormat.parse(timeString)
            
            val calendar = Calendar.getInstance()
            val timeCalendar = Calendar.getInstance().apply {
                timeInMillis = time?.time ?: 0
            }
            
            // Set to tomorrow
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing tomorrow's time: $timeString", e)
            0L
        }
    }

    /**
     * Create notification with prayer information
     */
    private fun createPrayerNotification(prayerInfo: PrayerInfo) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🕌 ${prayerInfo.title}")
        .setContentText("${prayerInfo.countdown} remaining")
        .setSmallIcon(R.drawable.moon)
        .setOngoing(true)
        .setAutoCancel(false)
        .setContentIntent(createMainActivityIntent())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("${prayerInfo.countdown} remaining"))
        .build()

    /**
     * Create notification when no prayer times are available
     */
    private fun createNoPrayerTimesNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🕌 Prayer Times")
        .setContentText("Prayer schedule not available")
        .setSmallIcon(R.drawable.moon)
        .setOngoing(true)
        .setAutoCancel(false)
        .setContentIntent(createMainActivityIntent())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .build()

    /**
     * Data class to hold prayer information
     */
    private data class PrayerInfo(
        val name: String,
        val time: String,
        val countdown: String,
        val title: String
    )
}
