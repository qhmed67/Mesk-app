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
import com.example.masjd2.services.DailyRescheduleWorker
import com.example.masjd2.services.PrayerNotificationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DownloadProgressActivity : ComponentActivity() {
    
    private lateinit var prayerRepository: PrayerRepository
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var method: Int = 3
    private var isReload: Boolean = false
    private var isDownloadComplete by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        method = intent.getIntExtra("method", 3)
        isReload = intent.getBooleanExtra("isReload", false)
        
        prayerRepository = PrayerRepository(this)
        
        setContent {
            DownloadProgressScreen(
                isReload = isReload,
                isDownloadComplete = isDownloadComplete,
                onDownloadComplete = { 
                    lifecycleScope.launch {
                        if (!isReload) {
                            prayerRepository.markFirstLaunchCompleted()
                            startPersistentNotificationService()
                            startActivity(Intent(this@DownloadProgressActivity, PermissionsActivity::class.java))
                        } else {
                            startPersistentNotificationService()
                            startActivity(Intent(this@DownloadProgressActivity, MainActivity::class.java))
                        }
                        finish()
                    }
                },
                onDownloadError = { error: String ->
                    Toast.makeText(this, "Download failed: $error", Toast.LENGTH_LONG).show()
                    finish()
                }
            )
        }
        
        startDownloadProcess()
    }
    
    private fun startDownloadProcess() {
        lifecycleScope.launch {
            try {
                val currentYear = LocalDate.now().year
                val result = prayerRepository.fetchAndSavePrayerTimesForYear(latitude, longitude, currentYear)
                
                if (result) {
                    val (country, city) = prayerRepository.getLocationInfo(latitude, longitude)
                    val methodName = prayerRepository.getCalculationMethodName(method)
                    
                    prayerRepository.saveUserPreferences(
                        country = country,
                        city = city,
                        calculationMethod = methodName,
                        latitude = latitude,
                        longitude = longitude,
                        isFirstLaunch = !isReload
                    )
                    
                    scheduleAthanAlarms()
                    
                    isDownloadComplete = true
                    Toast.makeText(this@DownloadProgressActivity, "Prayer times downloaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    isDownloadComplete = true
                    Toast.makeText(this@DownloadProgressActivity, "Failed to download prayer times", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                isDownloadComplete = true
                Toast.makeText(this@DownloadProgressActivity, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private suspend fun scheduleAthanAlarms() {
        try {
            prayerRepository.initializeAthanSettings()
            
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
            
            if (todayPrayerTimes != null) {
                val athanSettings = prayerRepository.getAthanSettings()
                if (athanSettings?.isAthanEnabled == true) {
                    val alarmManager = AthanAlarmManager(this@DownloadProgressActivity)
                    alarmManager.scheduleAllPrayerAlarms(
                        todayPrayerTimes,
                        athanSettings.athanVolume,
                        athanSettings.customAthanPath
                    )
                    DailyRescheduleWorker.enqueueDailyReschedule(this@DownloadProgressActivity)
                    Log.d("DownloadProgressActivity", "Scheduled Athan alarms for today")
                } else {
                    Log.d("DownloadProgressActivity", "Athan is disabled - not scheduling alarms")
                }
            } else {
                Log.w("DownloadProgressActivity", "No prayer times found for today")
            }
        } catch (e: Exception) {
            Log.e("DownloadProgressActivity", "Error scheduling Athan alarms: ${e.message}", e)
        }
    }

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
    isDownloadComplete: Boolean = false,
    onDownloadComplete: () -> Unit,
    onDownloadError: (String) -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var currentStep by remember { mutableStateOf("Initializing download...") }
    var isCompleted by remember { mutableStateOf(false) }
    
    LaunchedEffect(isDownloadComplete) {
        if (isDownloadComplete) {
            isCompleted = true
            progress = 1.0f
            currentStep = "Download completed!"
            delay(1000)
            onDownloadComplete()
        }
    }
    
    LaunchedEffect(Unit) {
        if (!isDownloadComplete) {
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
                delay(800)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.starry_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🕌", fontSize = 64.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text(
                text = if (isReload) "Reloading Prayer Times" else "Welcome to Prayer Times",
                fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                color = Color.White, modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = if (isReload) "Updating prayer times for the year..." else "Downloading prayer times for the year...",
                fontSize = 16.sp, textAlign = TextAlign.Center, color = Color(0xFFB0BEC5),
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF20424f).copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).padding(bottom = 16.dp)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF87CEEB) else Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = currentStep, fontSize = 14.sp, textAlign = TextAlign.Center, color = Color(0xFFB0BEC5))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isCompleted) {
                        Text(
                            text = if (isReload) "Prayer times updated successfully!" else "Prayer times are now ready!",
                            fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF87CEEB)
                        )
                    } else {
                        Text(text = "This may take a few moments...", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF20424f).copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "What's happening?", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF87CEEB), modifier = Modifier.padding(bottom = 8.dp))
                    Text(text = if (isReload) "• Updating prayer times for all 12 months" else "• Downloading prayer times for all 12 months", fontSize = 14.sp, color = Color(0xFFB0BEC5))
                    Text(text = "• Calculating times based on your location", fontSize = 14.sp, color = Color(0xFFB0BEC5))
                    Text(text = if (isReload) "• Updating data for offline use" else "• Saving data for offline use", fontSize = 14.sp, color = Color(0xFFB0BEC5))
                    Text(text = if (isReload) "• Updating your preferences" else "• Setting up your preferences", fontSize = 14.sp, color = Color(0xFFB0BEC5))
                }
            }
        }
    }
}
