package com.example.masjd2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import android.util.Log
import com.example.masjd2.MainActivity
import com.example.masjd2.R
import com.example.masjd2.repository.PrayerRepository
import com.example.masjd2.services.AthanAlarmManager
import com.example.masjd2.services.PrayerNotificationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Download progress activity for showing prayer times download progress
 */
class DownloadProgressActivity : ComponentActivity() {
    
    private lateinit var prayerRepository: PrayerRepository
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var method: Int = 3
    private var isReload: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Get data from intent
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        method = intent.getIntExtra("method", 3)
        isReload = intent.getBooleanExtra("isReload", false)
        
        prayerRepository = PrayerRepository(this)
        
        setContent {
            DownloadProgressScreen(
                isReload = isReload,
                onDownloadComplete = { 
                    lifecycleScope.launch {
                        if (!isReload) {
                            // Mark first launch as completed only for first time setup
                            prayerRepository.markFirstLaunchCompleted()
                            // Start persistent notification service
                            startPersistentNotificationService()
                            // Navigate to permissions activity for first-time setup
                            startActivity(Intent(this@DownloadProgressActivity, PermissionsActivity::class.java))
                        } else {
                            // For reload, start notification service and go directly to main activity
                            startPersistentNotificationService()
                            startActivity(Intent(this@DownloadProgressActivity, MainActivity::class.java))
                        }
                        finish()
                    }
                },
                onDownloadError = { error ->
                    Toast.makeText(this, "Download failed: $error", Toast.LENGTH_LONG).show()
                    finish()
                }
            )
        }
        
        // Start download process
        startDownloadProcess()
    }
    
    private fun startDownloadProcess() {
        lifecycleScope.launch {
            try {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val result = prayerRepository.fetchAndSavePrayerTimesForYear(latitude, longitude, currentYear)
                
                if (result) {
                    // Get location info for saving preferences
                    val (country, city) = prayerRepository.getLocationInfo(latitude, longitude)
                    val methodName = prayerRepository.getCalculationMethodName(method)
                    
                    // Save user preferences
                    prayerRepository.saveUserPreferences(
                        country = country,
                        city = city,
                        calculationMethod = methodName,
                        latitude = latitude,
                        longitude = longitude,
                        isFirstLaunch = !isReload // Only true for first time setup, false for reload
                    )
                    
                    // Schedule Athan alarms for today's prayer times
                    scheduleAthanAlarms()
                    
                    // Download completed successfully
                    Toast.makeText(this@DownloadProgressActivity, "Prayer times downloaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    // Download failed
                    Toast.makeText(this@DownloadProgressActivity, "Failed to download prayer times", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DownloadProgressActivity, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Schedule Athan alarms for today's prayer times
     */
    private suspend fun scheduleAthanAlarms() {
        try {
            // Initialize Athan settings if not exists
            prayerRepository.initializeAthanSettings()
            
            // Get today's prayer times
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
            
            if (todayPrayerTimes != null) {
                val athanSettings = prayerRepository.getAthanSettings()
                if (athanSettings?.isAthanEnabled == true) {
                    val alarmManager = AthanAlarmManager(this@DownloadProgressActivity)
                    alarmManager.scheduleAllPrayerAlarms(todayPrayerTimes)
                    android.util.Log.d("DownloadProgressActivity", "Scheduled Athan alarms for today")
                } else {
                    android.util.Log.d("DownloadProgressActivity", "Athan is disabled - not scheduling alarms")
                }
            } else {
                android.util.Log.w("DownloadProgressActivity", "No prayer times found for today - cannot schedule alarms")
            }
        } catch (e: Exception) {
            android.util.Log.e("DownloadProgressActivity", "Error scheduling Athan alarms: ${e.message}", e)
        }
    }

    /**
     * Start the persistent notification service for prayer countdown
     */
    private fun startPersistentNotificationService() {
        try {
            val serviceIntent = Intent(this, PrayerNotificationService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent)
            Log.d("DownloadProgressActivity", "Started persistent notification service")
        } catch (e: Exception) {
            Log.e("DownloadProgressActivity", "Error starting persistent notification service: ${e.message}", e)
        }
    }
}

@Composable
fun DownloadProgressScreen(
    isReload: Boolean = false,
    onDownloadComplete: () -> Unit,
    onDownloadError: (String) -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var currentStep by remember { mutableStateOf("Initializing download...") }
    var isCompleted by remember { mutableStateOf(false) }
    
    // Simulate download progress
    LaunchedEffect(Unit) {
        val steps = listOf(
            "Connecting to AlAdhan API..." to 0.1f,
            "Downloading January prayer times..." to 0.2f,
            "Downloading February prayer times..." to 0.3f,
            "Downloading March prayer times..." to 0.4f,
            "Downloading April prayer times..." to 0.5f,
            "Downloading May prayer times..." to 0.6f,
            "Downloading June prayer times..." to 0.7f,
            "Downloading July prayer times..." to 0.8f,
            "Downloading August prayer times..." to 0.9f,
            "Downloading September prayer times..." to 0.95f,
            "Downloading October prayer times..." to 0.98f,
            "Downloading November prayer times..." to 0.99f,
            "Downloading December prayer times..." to 1.0f,
            "Saving to database..." to 1.0f,
            "Download completed!" to 1.0f
        )
        
        for ((step, progressValue) in steps) {
            currentStep = step
            progress = progressValue
            delay(800) // Simulate download time
        }
        
        isCompleted = true
        delay(1000) // Show completion message
        onDownloadComplete()
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Starry background
        Image(
            painter = painterResource(id = R.drawable.starry_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // App Icon/Logo
        Text(
            text = "🕌",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = if (isReload) "Reloading Prayer Times" else "Welcome to Prayer Times",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = if (isReload) "Updating prayer times for the year..." else "Downloading prayer times for the year...",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFFB0BEC5),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF20424f).copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(bottom = 16.dp)
                )
                
                // Progress Percentage
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color(0xFF87CEEB) else Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current Step
                Text(
                    text = currentStep,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFB0BEC5)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Additional Info
                if (isCompleted) {
                    Text(
                        text = if (isReload) "Prayer times updated successfully!" else "Prayer times are now ready!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF87CEEB)
                    )
                } else {
                    Text(
                        text = "This may take a few moments...",
                        fontSize = 12.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF20424f).copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isReload) "What's happening?" else "What's happening?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF87CEEB),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = if (isReload) "• Updating prayer times for all 12 months" else "• Downloading prayer times for all 12 months",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "• Calculating times based on your location",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = if (isReload) "• Updating data for offline use" else "• Saving data for offline use",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = if (isReload) "• Updating your preferences" else "• Setting up your preferences",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }
    }
}
}
