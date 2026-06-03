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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class PrayerNotificationService : Service() {

    companion object {
        private const val TAG = "PrayerNotificationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "prayer_countdown_channel"
        private const val CHANNEL_NAME = "Prayer Time Countdown"
        
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
        startForeground(NOTIFICATION_ID, createInitialNotification())
        startCountdownUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows countdown to next prayer time"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

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

    private fun createMainActivityIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun startCountdownUpdates() {
        serviceJob?.cancel()
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    updateNotificationWithPrayerInfo()
                    delay(60_000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating prayer notification: ${e.message}", e)
                    delay(60_000)
                }
            }
        }
    }

    private suspend fun updateNotificationWithPrayerInfo() {
        try {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
            
            if (todayPrayerTimes != null) {
                val prayerInfo = getNextPrayerInfo(todayPrayerTimes)
                val notification = createPrayerNotification(prayerInfo)
                withContext(Dispatchers.Main) {
                    notificationManager?.notify(NOTIFICATION_ID, notification)
                }
                Log.d(TAG, "Updated notification: ${prayerInfo.title}")
            } else {
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

    private suspend fun getNextPrayerInfo(prayerTimes: PrayerEntity): PrayerInfo {
        val nowUtc = System.currentTimeMillis()
        
        val prayers = listOf(
            Triple("fajr", prayerTimes.fajrUtc, prayerTimes.fajr),
            Triple("dhuhr", prayerTimes.dhuhrUtc, prayerTimes.dhuhr),
            Triple("asr", prayerTimes.asrUtc, prayerTimes.asr),
            Triple("maghrib", prayerTimes.maghribUtc, prayerTimes.maghrib),
            Triple("isha", prayerTimes.ishaUtc, prayerTimes.isha)
        )
        
        for ((prayerKey, utcMillis, displayTime) in prayers) {
            if (utcMillis > 0L && utcMillis > nowUtc) {
                val timeUntil = utcMillis - nowUtc
                val formattedTime = formatTime12Hour(displayTime)
                
                return PrayerInfo(
                    name = PRAYER_NAMES[prayerKey] ?: prayerKey,
                    time = formattedTime,
                    countdown = formatCountdown(timeUntil),
                    title = "Next: ${PRAYER_NAMES[prayerKey]} today at $formattedTime"
                )
            }
        }
        
        return getTomorrowFajrInfo()
    }
    
    private fun formatTime12Hour(timeString: String): String {
        return try {
            if (timeString.contains("AM", ignoreCase = true) || timeString.contains("PM", ignoreCase = true)) {
                timeString
            } else {
                val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
                val outputFormat = SimpleDateFormat("h:mm a", Locale.US)
                val time = inputFormat.parse(timeString)
                outputFormat.format(time ?: Date())
            }
        } catch (e: Exception) {
            timeString
        }
    }

    private fun formatCountdown(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Soon"
        }
    }

    private suspend fun getTomorrowFajrInfo(): PrayerInfo {
        return try {
            val nowUtc = System.currentTimeMillis()
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
            
            if (todayPrayerTimes != null && todayPrayerTimes.fajrUtc > nowUtc) {
                val timeUntil = todayPrayerTimes.fajrUtc - nowUtc
                return PrayerInfo(
                    name = "Fajr",
                    time = formatTime12Hour(todayPrayerTimes.fajr),
                    countdown = formatCountdown(timeUntil),
                    title = "Next: Fajr today at ${formatTime12Hour(todayPrayerTimes.fajr)}"
                )
            }
            
            val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val tomorrowPrayerTimes = prayerRepository.getPrayerTimesForDate(tomorrow)
            
            if (tomorrowPrayerTimes != null && tomorrowPrayerTimes.fajrUtc > 0L) {
                val timeUntil = tomorrowPrayerTimes.fajrUtc - nowUtc
                return PrayerInfo(
                    name = "Fajr",
                    time = formatTime12Hour(tomorrowPrayerTimes.fajr),
                    countdown = formatCountdown(timeUntil),
                    title = "Next: Fajr tomorrow at ${formatTime12Hour(tomorrowPrayerTimes.fajr)}"
                )
            } else {
                PrayerInfo(
                    name = "Fajr", time = "Unknown", countdown = "Data unavailable",
                    title = "Next: Fajr tomorrow (time unavailable)"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tomorrow's Fajr info: ${e.message}", e)
            PrayerInfo(
                name = "Fajr", time = "Unknown", countdown = "Error",
                title = "Next: Fajr tomorrow (error loading time)"
            )
        }
    }

    private fun createPrayerNotification(prayerInfo: PrayerInfo) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🕌 ${prayerInfo.title}")
        .setContentText("${prayerInfo.countdown} remaining")
        .setSmallIcon(R.drawable.moon)
        .setOngoing(true)
        .setAutoCancel(false)
        .setContentIntent(createMainActivityIntent())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setStyle(NotificationCompat.BigTextStyle().bigText("${prayerInfo.countdown} remaining"))
        .build()

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

    private data class PrayerInfo(
        val name: String,
        val time: String,
        val countdown: String,
        val title: String
    )
}
