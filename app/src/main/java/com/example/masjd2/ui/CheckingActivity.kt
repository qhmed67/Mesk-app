package com.example.masjd2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.masjd2.MainActivity
import com.example.masjd2.R
import com.example.masjd2.repository.PrayerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Checking activity that appears first to check database status
 * and decide whether to show welcome page or main page
 */
class CheckingActivity : ComponentActivity() {

    private lateinit var prayerRepository: PrayerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("CheckingActivity", "onCreate called - checking database status")
        prayerRepository = PrayerRepository(this)

        setContent {
            CheckingScreen(
                prayerRepository = prayerRepository,
                onNavigateToMain = {
                    Log.d("CheckingActivity", "Navigating to MainActivity")
                    startActivity(Intent(this@CheckingActivity, MainActivity::class.java))
                    finish()
                },
                onNavigateToWelcome = {
                    Log.d("CheckingActivity", "Navigating to FirstLaunchActivity")
                    startActivity(Intent(this@CheckingActivity, FirstLaunchActivity::class.java))
                    finish()
                }
            )
        }

    }
}

@Composable
fun CheckingScreen(
    prayerRepository: PrayerRepository,
    onNavigateToMain: () -> Unit,
    onNavigateToWelcome: () -> Unit
) {
    var checkingStatus by remember { mutableStateOf("Checking database...") }
    var showResults by remember { mutableStateOf(false) }
    var needsUpdate by remember { mutableStateOf(false) }
    var isFirstTime by remember { mutableStateOf(false) }

    // Check database status
    LaunchedEffect(Unit) {
        try {
            delay(1000)
            checkingStatus = "Checking prayer times..."
            
            // Check if database has any prayer times at all
            val hasAnyPrayerTimes = prayerRepository.hasAnyPrayerTimes()
            Log.d("CheckingActivity", "Database has any prayer times: $hasAnyPrayerTimes")

            if (!hasAnyPrayerTimes) {
                // Database is completely empty - automatically navigate to first launch
                Log.d("CheckingActivity", "Database is empty - automatically navigating to first launch")
                delay(1000)
                checkingStatus = "First time setup needed..."
                delay(1000)
                // Automatically navigate to first launch without showing button
                onNavigateToWelcome()
                return@LaunchedEffect
            }

            delay(1000)
            checkingStatus = "Verifying data..."

            // Check if today's prayer times exist
            val hasPrayerTimesForToday = prayerRepository.hasPrayerTimesForToday()
            Log.d("CheckingActivity", "Has prayer times for today: $hasPrayerTimesForToday")

            if (!hasPrayerTimesForToday) {
                // Database has data but not for today - automatically navigate to update
                Log.d("CheckingActivity", "No prayer times for today - automatically navigating to update")
                delay(1000)
                checkingStatus = "Update needed..."
                delay(1000)
                // Automatically navigate to first launch for update without showing button
                onNavigateToWelcome()
                return@LaunchedEffect
            }

            // Database has data and today's prayer times exist - automatically navigate to main page
            Log.d("CheckingActivity", "Database has data and today's prayer times - automatically navigating to main page")
            delay(1000)
            checkingStatus = "Ready to go!"
            delay(1000)
            // Automatically navigate to main page without showing button
            onNavigateToMain()

        } catch (e: Exception) {
            Log.e("CheckingActivity", "Error checking database: ${e.message}", e)
            delay(1000)
            checkingStatus = "Error occurred..."
            delay(1000)
            // Automatically navigate to first launch on error
            onNavigateToWelcome()
        }
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
        // Mosque Icon
        Text(
            text = "🕌",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Welcome Title
        Text(
            text = "Welcome to Prayer Times",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Checking Status
        if (!showResults) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp),
                    color = Color.White
                )
                Text(
                    text = checkingStatus,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFB0BEC5)
                )
            }
        } else {
        // Show redirecting message (this should rarely be seen since we auto-navigate)
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
                Text(
                    text = "Redirecting...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF87CEEB)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Please wait while we set up your prayer times...",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFB0BEC5)
                )
            }
        }
        }
    }
}
}
