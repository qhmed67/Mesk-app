package com.example.masjd2.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.example.masjd2.MainActivity
import com.example.masjd2.R
import com.example.masjd2.receivers.AthanStopReceiver
import java.io.File

/**
 * Foreground service that plays Athan audio and shows notification
 */
class AthanService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var prayerName: String = ""
    private var alarmId: Int = -1
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var volumeClickChecker: VolumeClickChecker? = null
    private var mediaSession: MediaSession? = null

    companion object {
        private const val TAG = "AthanService"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "athan_channel"
        private const val CHANNEL_NAME = "Athan Notifications"
        
        // Action for stopping Athan
        const val ACTION_STOP_ATHAN = "com.example.masjd2.STOP_ATHAN"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AthanService created")
        createNotificationChannel()
        
        // Initialize AudioManager for focus management
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Acquire wake lock to keep device awake during Athan
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AthanService::WakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
        
        // Initialize volume click checker
        volumeClickChecker = VolumeClickChecker(this)
        Log.d(TAG, "Volume click checker initialized")
        
        // Initialize MediaSession for volume button handling when locked
        setupMediaSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AthanService started")
        
        if (intent?.action == ACTION_STOP_ATHAN) {
            Log.d(TAG, "Stop Athan action received")
            stopAthan()
            return START_NOT_STICKY
        }
        
        // Get prayer information from intent
        prayerName = intent?.getStringExtra("prayer_name") ?: "Prayer"
        alarmId = intent?.getIntExtra("alarm_id", -1) ?: -1
        val athanVolume = intent?.getFloatExtra("athan_volume", 1.0f) ?: 1.0f
        val customAthanPath = intent?.getStringExtra("custom_athan_path")
        
        Log.d(TAG, "Starting Athan for $prayerName with volume $athanVolume")
        
        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Play Athan audio
        playAthan(customAthanPath, athanVolume)
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "AthanService destroyed")
        
        // Stop volume click checker
        volumeClickChecker?.stopChecking()
        
        // Clean up MediaSession
        mediaSession?.release()
        mediaSession = null
        
        stopAthan()
        super.onDestroy()
    }

    /**
     * Create notification channel for Athan notifications
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for prayer time Athan"
                setSound(null, null) // We handle sound ourselves
                enableVibration(false) // No vibration, only sound
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification for Athan
     */
    private fun createNotification(): Notification {
        // Intent to open main activity when notification is tapped
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent to stop Athan
        val stopIntent = Intent(this, AthanStopReceiver::class.java).apply {
            action = ACTION_STOP_ATHAN
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Athan - $prayerName Prayer")
            .setContentText("It's time for $prayerName prayer")
            .setSmallIcon(R.drawable.moon) // status bar small icon
            .setContentIntent(mainPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground, // You may want to create a stop icon
                "Stop Athan",
                stopPendingIntent
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    /**
     * Play Athan audio with proper audio focus management
     */
    private fun playAthan(customAthanPath: String?, volume: Float) {
        try {
            // Request audio focus for ALARM stream
            if (!requestAudioFocus()) {
                Log.e(TAG, "Failed to get audio focus - cannot play Athan")
                stopAthan()
                return
            }
            
            mediaPlayer?.release() // Release any existing player
            mediaPlayer = MediaPlayer()
            
            // Set audio attributes for alarm stream
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaPlayer?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                mediaPlayer?.setAudioStreamType(AudioManager.STREAM_ALARM)
            }
            
            // Set data source (custom file or default sound)
            if (customAthanPath != null && File(customAthanPath).exists()) {
                Log.d(TAG, "Playing custom Athan from: $customAthanPath")
                mediaPlayer?.setDataSource(customAthanPath)
            } else {
                Log.d(TAG, "Playing default Athan sound")
                // Use default notification sound as fallback
                val defaultUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                mediaPlayer?.setDataSource(this, defaultUri)
            }
            
            // Set volume
            mediaPlayer?.setVolume(volume, volume)
            
            // Set looping to play until manually stopped
            mediaPlayer?.isLooping = true
            
            // Set completion listener (in case looping fails)
            mediaPlayer?.setOnCompletionListener {
                Log.d(TAG, "Athan playback completed")
            }
            
            // Set error listener
            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                stopAthan()
                true
            }
            
            // Prepare and start playback
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            
            // Start volume click checker when Athan starts
            volumeClickChecker?.startChecking()
            
            // Activate MediaSession for volume button handling when locked
            mediaSession?.isActive = true
            mediaSession?.setPlaybackState(
                PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_STOP)
                    .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                    .build()
            )
            
            Log.d(TAG, "Athan playback started for $prayerName")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing Athan: ${e.message}", e)
            stopAthan()
        }
    }

    /**
     * Stop Athan playback and service
     */
    fun stopAthan() {
        Log.d(TAG, "Stopping Athan")
        
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaPlayer: ${e.message}", e)
        }
        
        // Stop volume click checker
        volumeClickChecker?.stopChecking()
        
        // Deactivate MediaSession
        mediaSession?.isActive = false
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_STOPPED, 0, 1.0f)
                .build()
        )
        
        // Release audio focus
        releaseAudioFocus()
        
        // Release wake lock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
        
        // Stop foreground service
        stopForeground(true)
        stopSelf()
    }
    
    /**
     * Setup MediaSession for volume button handling when screen is locked
     */
    private fun setupMediaSession() {
        try {
            mediaSession = MediaSession(this, "AthanMediaSession")
            
            mediaSession?.setCallback(object : MediaSession.Callback() {
                override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                    val keyEvent = mediaButtonEvent.getParcelableExtra<android.view.KeyEvent>(Intent.EXTRA_KEY_EVENT)
                    if (keyEvent?.action == android.view.KeyEvent.ACTION_DOWN) {
                        when (keyEvent.keyCode) {
                            android.view.KeyEvent.KEYCODE_VOLUME_DOWN, android.view.KeyEvent.KEYCODE_VOLUME_UP -> {
                                Log.d(TAG, "MediaSession: Volume button pressed - stopping Athan IMMEDIATELY")
                                stopAthan()
                                return true // Consume the event
                            }
                        }
                    }
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }
                
                override fun onStop() {
                    Log.d(TAG, "MediaSession: Stop requested - stopping Athan")
                    stopAthan()
                }
            })
            
            Log.d(TAG, "MediaSession setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up MediaSession: ${e.message}", e)
        }
    }
    
    /**
     * Request audio focus for ALARM stream
     */
    private fun requestAudioFocus(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> {
                                Log.d(TAG, "Audio focus lost - stopping Athan")
                                stopAthan()
                            }
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                                Log.d(TAG, "Audio focus lost transient - stopping Athan")
                                stopAthan()
                            }
                        }
                    }
                    .build()
                
                val result = audioManager?.requestAudioFocus(audioFocusRequest!!)
                Log.d(TAG, "Audio focus request result: $result")
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                val result = audioManager?.requestAudioFocus(
                    { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> {
                                Log.d(TAG, "Audio focus lost - stopping Athan")
                                stopAthan()
                            }
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                                Log.d(TAG, "Audio focus lost transient - stopping Athan")
                                stopAthan()
                            }
                        }
                    },
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN
                )
                Log.d(TAG, "Audio focus request result: $result")
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting audio focus: ${e.message}", e)
            false
        }
    }
    
    /**
     * Release audio focus
     */
    private fun releaseAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    audioManager?.abandonAudioFocusRequest(request)
                    audioFocusRequest = null
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
            Log.d(TAG, "Audio focus released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio focus: ${e.message}", e)
        }
    }
}

/**
 * Volume Click Checker - Monitors for any volume button clicks when Athan is playing
 */
class VolumeClickChecker(private val athanService: AthanService) {
    
    private val TAG = "VolumeClickChecker"
    private var isChecking = false
    private var volumeReceiver: BroadcastReceiver? = null
    
    /**
     * Start checking for volume button clicks
     */
    fun startChecking() {
        if (isChecking) return
        
        try {
            isChecking = true
            Log.d(TAG, "Starting volume click monitoring")
            
            volumeReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_MEDIA_BUTTON -> {
                            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                            if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                                when (keyEvent.keyCode) {
                                    KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                                        Log.d(TAG, "Volume button clicked - stopping Athan IMMEDIATELY")
                                        athanService.stopAthan()
                                    }
                                }
                            }
                        }
                        "android.media.VOLUME_CHANGED_ACTION" -> {
                            Log.d(TAG, "Volume changed - stopping Athan IMMEDIATELY")
                            athanService.stopAthan()
                        }
                    }
                }
            }
            
            // Register for volume events with maximum priority
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_MEDIA_BUTTON)
                addAction("android.media.VOLUME_CHANGED_ACTION")
                priority = IntentFilter.SYSTEM_HIGH_PRIORITY + 1000 // Maximum priority
            }
            athanService.registerReceiver(volumeReceiver, filter)
            Log.d(TAG, "Volume click checker registered with MAXIMUM priority")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting volume click checker: ${e.message}", e)
        }
    }
    
    /**
     * Stop checking for volume button clicks
     */
    fun stopChecking() {
        if (!isChecking) return
        
        try {
            isChecking = false
            volumeReceiver?.let { receiver ->
                athanService.unregisterReceiver(receiver)
                volumeReceiver = null
            }
            Log.d(TAG, "Volume click checker stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping volume click checker: ${e.message}", e)
        }
    }
}
