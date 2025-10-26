package com.example.masjd2.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import com.example.masjd2.ui.theme.Masjd2Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity for requesting all Athan-related permissions in one place
 */
class PermissionsActivity : ComponentActivity() {

    companion object {
        private const val TAG = "PermissionsActivity"
        const val EXTRA_FROM_SETTINGS = "from_settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val fromSettings = intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false)

        setContent {
            Masjd2Theme {
                PermissionsScreen(
                    fromSettings = fromSettings,
                    onAllPermissionsGranted = {
                        // Navigate to MainActivity
                        startActivity(Intent(this@PermissionsActivity, MainActivity::class.java))
                        finish()
                    },
                    onSkip = {
                        // Navigate to MainActivity anyway
                        startActivity(Intent(this@PermissionsActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    /**
     * Check if exact alarm permission is granted (Android 12+)
     */
    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Not required on older versions
        }
    }

    /**
     * Check if battery optimization is disabled
     */
    private fun isBatteryOptimizationDisabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true // Not applicable on older versions
        }
    }

    /**
     * Request exact alarm permission
     */
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening exact alarm settings: ${e.message}", e)
                Toast.makeText(this, "Please enable 'Alarms & reminders' in app settings", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Request battery optimization whitelist
     */
    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening battery optimization settings: ${e.message}", e)
                // Fallback to battery settings
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                } catch (e2: Exception) {
                    Toast.makeText(this, "Please disable battery optimization in app settings", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Composable
    fun PermissionsScreen(
        fromSettings: Boolean,
        onAllPermissionsGranted: () -> Unit,
        onSkip: () -> Unit
    ) {
        var exactAlarmGranted by remember { mutableStateOf(canScheduleExactAlarms()) }
        var batteryOptimizationDisabled by remember { mutableStateOf(isBatteryOptimizationDisabled()) }
        var isCheckingPermissions by remember { mutableStateOf(false) }

        // Periodically check permissions when user returns from settings
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000) // Check every second
                exactAlarmGranted = canScheduleExactAlarms()
                batteryOptimizationDisabled = isBatteryOptimizationDisabled()
            }
        }

        // Auto-navigate when all permissions are granted
        LaunchedEffect(exactAlarmGranted, batteryOptimizationDisabled) {
            if (exactAlarmGranted && batteryOptimizationDisabled && !fromSettings) {
                delay(1000) // Small delay to show success state
                onAllPermissionsGranted()
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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header
            Text(
                text = "🕌",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = if (fromSettings) "Athan Permissions" else "Setup Athan Notifications",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Text(
                text = if (fromSettings) 
                    "Manage permissions for reliable Athan notifications"
                else 
                    "To ensure your Athan plays at the exact prayer times, we need a few permissions",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Permissions Cards
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionCard(
                    title = "Alarms & Reminders",
                    description = "Required for precise prayer time notifications on Android 12+",
                    isGranted = exactAlarmGranted,
                    onRequest = { requestExactAlarmPermission() }
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermissionCard(
                    title = "Battery Optimization",
                    description = "Prevents Android from delaying Athan notifications to save battery",
                    isGranted = batteryOptimizationDisabled,
                    onRequest = { requestBatteryOptimization() }
                )
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF20424f).copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Why these permissions?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Android restricts background apps to save battery\n• Prayer apps need exemptions for religious obligations\n• All major prayer apps (Muslim Pro, Athan Pro) use these same permissions\n• Without them, Athan may be delayed by 15+ minutes",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (exactAlarmGranted && batteryOptimizationDisabled) {
                // All permissions granted
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF20424f).copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All Set! ✅",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Your Athan notifications will work reliably",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (fromSettings) {
                    Button(
                        onClick = { finish() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                } else {
                    Button(
                        onClick = onAllPermissionsGranted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue to Prayer Times")
                    }
                }
            } else {
                // Some permissions missing
                Button(
                    onClick = {
                        isCheckingPermissions = true
                        lifecycleScope.launch {
                            delay(500)
                            exactAlarmGranted = canScheduleExactAlarms()
                            batteryOptimizationDisabled = isBatteryOptimizationDisabled()
                            isCheckingPermissions = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingPermissions
                ) {
                    if (isCheckingPermissions) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checking...")
                    } else {
                        Text("Check Permissions")
                    }
                }

                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for Now")
                }

                Text(
                    text = "⚠️ Skipping may cause delayed or missed Athan notifications",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) 
                Color(0xFF20424f).copy(alpha = 0.9f)
            else 
                Color(0xFF20424f).copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGranted) Color.White else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (!isGranted) {
                OutlinedButton(onClick = onRequest) {
                    Text("Grant")
                }
            } else {
                Text(
                    text = "✓ Granted",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
}
