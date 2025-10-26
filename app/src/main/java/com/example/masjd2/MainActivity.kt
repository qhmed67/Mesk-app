package com.example.masjd2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.lifecycle.lifecycleScope
import com.example.masjd2.data.db.PrayerEntity
import com.example.masjd2.data.db.UserPreferencesEntity
import com.example.masjd2.repository.PrayerRepository
import com.example.masjd2.services.AthanAlarmManager
import com.example.masjd2.services.PrayerNotificationService
import com.example.masjd2.ui.PermissionsActivity
import com.example.masjd2.ui.SettingsActivity
import com.example.masjd2.ui.theme.Masjd2Theme
import com.example.masjd2.R
import android.view.KeyEvent
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ResolvableApiException
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    
    private lateinit var prayerRepository: PrayerRepository
    
    // Location settings result launcher
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Location settings were enabled, start CompassCalibrationActivity first
            val intent = Intent(this, CompassCalibrationActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("MainActivity", "onCreate called - app is starting")
        prayerRepository = PrayerRepository(this)
        Log.d("MainActivity", "PrayerRepository created")

        // Initialize Athan settings and schedule alarms
        lifecycleScope.launch {
            // Check if critical permissions are missing
            if (shouldShowPermissionsPage()) {
                startActivity(Intent(this@MainActivity, PermissionsActivity::class.java))
                finish()
                return@launch
            }
            
            initializeAthanSystem()
            
            // Start persistent notification service
            startPersistentNotificationService()
        }

        setContent {
            Masjd2Theme {
                PrayerTimesApp(prayerRepository = prayerRepository)
            }
        }
        Log.d("MainActivity", "setContent called - UI should be showing")
    }

    /**
     * Initialize Athan system: settings and alarm scheduling
     */
    private suspend fun initializeAthanSystem() {
        try {
            // Initialize default Athan settings if not exists
            prayerRepository.initializeAthanSettings()
            
            // Schedule alarms for today's prayer times
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayPrayerTimes = prayerRepository.getPrayerTimesForDate(today)
            
            if (todayPrayerTimes != null) {
                val athanSettings = prayerRepository.getAthanSettings()
                if (athanSettings?.isAthanEnabled == true) {
                    val alarmManager = AthanAlarmManager(this)
                    alarmManager.scheduleAllPrayerAlarms(todayPrayerTimes)
                    Log.d("MainActivity", "Scheduled Athan alarms for today")
                } else {
                    Log.d("MainActivity", "Athan is disabled - not scheduling alarms")
                }
            } else {
                Log.w("MainActivity", "No prayer times found for today - cannot schedule alarms")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing Athan system: ${e.message}", e)
        }
    }

    /**
     * Check if we should show the permissions page
     */
    private fun shouldShowPermissionsPage(): Boolean {
        // Check if Athan is enabled first
        val athanEnabled = try {
            // This is a quick check - we'll do a proper async check later if needed
            true // Assume enabled for now, will be checked properly in PermissionsActivity
        } catch (e: Exception) {
            true
        }
        
        if (!athanEnabled) return false
        
        // Check exact alarm permission (Android 12+)
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        
        // Check battery optimization
        val batteryOptimizationDisabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true
        }
        
        // Show permissions page if any critical permission is missing
        val shouldShow = !exactAlarmGranted || !batteryOptimizationDisabled
        
        Log.d("MainActivity", "Permission check: exactAlarm=$exactAlarmGranted, batteryOpt=$batteryOptimizationDisabled, shouldShow=$shouldShow")
        
        return shouldShow
    }

    /**
     * Check and request exact alarm permission for Android 12+
     */
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MainActivity", "Exact alarm permission not granted - requesting permission")
                
                // Show explanation to user
                Toast.makeText(
                    this,
                    "Please allow 'Alarms & reminders' permission for Athan notifications to work properly",
                    Toast.LENGTH_LONG
                ).show()
                
                // Open system settings for exact alarm permission
                try {
                    val intent = android.content.Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error opening exact alarm settings: ${e.message}", e)
                    // Fallback to general app settings
                    val intent = android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            } else {
                Log.d("MainActivity", "Exact alarm permission already granted")
            }
        } else {
            Log.d("MainActivity", "Android version < 12 - exact alarm permission not required")
        }
    }

    /**
     * Check and request battery optimization whitelist for reliable alarms
     */
    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.w("MainActivity", "App not whitelisted from battery optimization - requesting whitelist")
                
                // Show explanation to user
                Toast.makeText(
                    this,
                    "Please disable battery optimization for reliable Athan notifications",
                    Toast.LENGTH_LONG
                ).show()
                
                // Request battery optimization whitelist
                try {
                    val intent = android.content.Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error opening battery optimization settings: ${e.message}", e)
                    // Fallback to battery optimization settings
                    try {
                        val intent = android.content.Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivity(intent)
                    } catch (e2: Exception) {
                        Log.e("MainActivity", "Error opening battery settings: ${e2.message}", e2)
                    }
                }
            } else {
                Log.d("MainActivity", "App already whitelisted from battery optimization")
            }
        } else {
            Log.d("MainActivity", "Android version < 6 - battery optimization not applicable")
        }
    }


    /**
     * Start the persistent notification service for prayer countdown
     */
    private fun startPersistentNotificationService() {
        try {
            val serviceIntent = Intent(this, PrayerNotificationService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent)
            Log.d("MainActivity", "Started persistent notification service")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting persistent notification service: ${e.message}", e)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                // Location settings were enabled, start CompassActivity
                val intent = Intent(this, CompassActivity::class.java)
                startActivity(intent)
            }
        }
    }
    
    /**
     * Check location settings using Google Play Services
     */
    fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(10000)
            .setMinUpdateIntervalMillis(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .build()
        
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // This forces the dialog to always show
        
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. Start CompassCalibrationActivity first
            val intent = Intent(this, CompassCalibrationActivity::class.java)
            startActivity(intent)
        }
        
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, show the Google Play Services dialog
                try {
                    exception.startResolutionForResult(
                        this,
                        1001
                    )
                } catch (sendEx: Exception) {
                    Log.e("MainActivity", "Error showing location settings dialog: ${sendEx.message}")
                }
            } else {
                Log.e("MainActivity", "Location settings error: ${exception.message}")
            }
        }
    }
}

@Composable
fun PrayerTimesApp(prayerRepository: PrayerRepository) {
    val context = LocalContext.current
    var showSettingsMenu by remember { mutableStateOf(false) }
    var expandedPrayer by remember { mutableStateOf<String?>(null) }
    
    // Track animation state to coordinate all animations
    var isAnimating by remember { mutableStateOf(false) }
    
    // Update animation state when expandedPrayer changes
    LaunchedEffect(expandedPrayer) {
        isAnimating = true
        delay(500) // Match animation duration
        isAnimating = false
    }

    Log.d("MainActivity", "PrayerTimesApp composable started")

    // Collect prayer times for today
    val todayPrayerTimes by prayerRepository.getTodayPrayerTimes().collectAsState(initial = null)

    // Collect user preferences
    val userPreferences by prayerRepository.getUserPreferences().collectAsState(initial = null)
    
           // No need to check database here - CheckingActivity handles that
           Log.d("MainActivity", "MainActivity started - showing prayer times")
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image - moved into Flutter widget tree for proper blur support
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Navy blue overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B2951).copy(alpha = 0.3f))
        )
        
            
        // Prayer Times Cards - no scrolling
        
        // Location fade based on expanded prayer state only
        val locationFadeAlpha by animateFloatAsState(
            targetValue = if (expandedPrayer != null) 0.8f else 1f, // Fade when any prayer is expanded
            animationSpec = tween(durationMillis = 300),
            label = "location_fade_animation"
        )
        
        // Location widget with integrated icon at the top - 40dp down from status bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location widget with integrated Moon Stars icon
            userPreferences?.let { prefs ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .alpha(locationFadeAlpha),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent.copy(alpha = 0.7f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Container background image
                        Image(
                            painter = painterResource(id = R.drawable.container),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Content overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                    Column(
                            modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${prefs.calculationMethod} (${prefs.city})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                                color = Color.White
                        )
                        // Country + Gregorian date inline
                        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
                        Text(
                            text = "${prefs.country}, $currentDate",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        }
                        
                        // Moon Stars icon integrated into location widget
                        Image(
                            painter = painterResource(id = R.drawable.moon_stars_24px), // Custom Moon Stars icon
                            contentDescription = "Moon Stars",
                            modifier = Modifier.size(24.dp)
                        )
                        }
                    }
                }
            }
        }
        
        // No scrolling - removed all scroll functionality
        
        val density = LocalDensity.current
        var parentHeightPx by remember { mutableStateOf(0) }
        var initialTopOffsetPx by remember { mutableStateOf<Int?>(null) }
        val isIshaExpanded = expandedPrayer == "isha"
        val ishaExtraShift by animateDpAsState(
            targetValue = if (isIshaExpanded) 56.dp else 0.dp,
            animationSpec = tween(durationMillis = 300),
            label = "isha_extra_shift"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .onGloballyPositioned { parentHeightPx = it.size.height }
        ) {
            todayPrayerTimes?.let { prayerTimes ->
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = with(density) { (initialTopOffsetPx ?: 0).toDp() })
                        .offset(y = -ishaExtraShift)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .onGloballyPositioned { coords ->
                            val contentHeight = coords.size.height
                            if (initialTopOffsetPx == null && parentHeightPx > 0) {
                                val centered = ((parentHeightPx - contentHeight) / 2).coerceAtLeast(0)
                                initialTopOffsetPx = centered
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PrayerCardWithMargin("الفجر", "Fajr", prayerTimes.fajr, "fajr", expandedPrayer, isAnimating) { expandedPrayer = it }
                    PrayerCardWithMargin("الظهر", "Dhuhr", prayerTimes.dhuhr, "dhuhr", expandedPrayer, isAnimating) { expandedPrayer = it }
                    PrayerCardWithMargin("العصر", "Asr", prayerTimes.asr, "asr", expandedPrayer, isAnimating) { expandedPrayer = it }
                    PrayerCardWithMargin("المغرب", "Maghrib", prayerTimes.maghrib, "maghrib", expandedPrayer, isAnimating) { expandedPrayer = it }
                    PrayerCardWithMargin("العشاء", "Isha", prayerTimes.isha, "isha", expandedPrayer, isAnimating) { expandedPrayer = it }
                }
            } ?: run {
                // Show loading or no data message
                Card(
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Loading prayer times...",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom navigation container - centered at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(vertical = 20.dp)
        ) {
            // Container with photo background - auto-sizing based on content
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                // Photo background
                Image(
                    painter = painterResource(id = R.drawable.container), // Use your container.jpg
                    contentDescription = null,
                    modifier = Modifier
                        .width(310.dp) // Very tiny increase
                        .height(70.dp) // Decreased height
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Navigation buttons overlay
                Row(
                    modifier = Modifier
                        .width(310.dp) // Match container width
                        .height(70.dp) // Match container height
                        .padding(horizontal = 20.dp), // Adjusted padding for perfect centering
                    horizontalArrangement = Arrangement.SpaceEvenly, // Equal spacing between all buttons
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // أذكار (Athkar) - Left
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color(0xFF20424f).copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { 
                            Log.d("MainActivity", "أذكار BUTTON CLICKED!")
                            try {
                                val intent = android.content.Intent(context, AzkarActivity::class.java)
                                context.startActivity(intent)
                                Log.d("MainActivity", "Azkar activity started")
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Error starting AzkarActivity: ${e.message}", e)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.menu_book_24px),
                        contentDescription = "أذكار",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Compass - Middle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color(0xFF20424f).copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { 
                            Log.d("MainActivity", "COMPASS BUTTON CLICKED!")
                            // Always show Google Play Services location dialog
                            (context as MainActivity).checkLocationSettings()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.explore_24px),
                        contentDescription = "Compass",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Settings - Right
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color(0xFF20424f).copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { 
                            Log.d("MainActivity", "BOTTOM SETTINGS BUTTON CLICKED!")
                            try {
                                val intent = android.content.Intent(context, SettingsActivity::class.java)
                                context.startActivity(intent)
                                Log.d("MainActivity", "Settings activity started from bottom bar")
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Error starting SettingsActivity from bottom bar: ${e.message}", e)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                }
            }
        }

    }
}

@Composable
fun getPrayerBackground(prayerType: String): Painter {
    return when (prayerType.lowercase()) {
        "fajr" -> painterResource(id = R.drawable.fajr)
        "dhuhr" -> painterResource(id = R.drawable.dhuhr)
        "asr" -> painterResource(id = R.drawable.asr)
        "maghrib" -> painterResource(id = R.drawable.maghrib)
        "isha" -> painterResource(id = R.drawable.isha)
        else -> painterResource(id = R.drawable.fajr) // Default to fajr
    }
}

@Composable
fun PrayerCard(arabicName: String, englishName: String, time: String, prayerType: String = "") {
    var isExpanded by remember { mutableStateOf(false) }
    
    // Animation for the dropping sign effect - starts behind prayer widget and drops down
    val slideOffset by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -1f,
        animationSpec = tween(durationMillis = 400),
        label = "drop_animation"
    )
    
    // Fade animation for smooth transition
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "fade_animation"
    )
    
    // Get specific sunnah text for each prayer
    val sunnahText = when (prayerType.lowercase()) {
        "fajr" -> "إثنتان قبل صلاة الفجر"
        "dhuhr" -> "أربع قبل الظهر [مثنى مثنى] تسليمتين و إثنتان بعد الظهر تسليمة واحدة"
        "asr" -> "لا يوجد"
        "maghrib" -> "إثنتان بعد المغرب"
        "isha" -> "إثنتان بعد العشاء"
        else -> "أربع قبل الظهر تسليمتين، وثنتان بعد الظهر تسليمة واحدة، وثنتان بعد المغرب، وثنتان بعد العشاء، وثنتان قبل صلاة الفجر،"
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        // Details box - positioned behind prayer widget, top sticks to prayer widget bottom
        if (isExpanded || alpha > 0f) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 80.dp + (slideOffset * 80).dp)
                    .alpha(alpha)
                    .drawWithContent {
                        drawContent()
                        // Draw custom border without top edge
                        val strokeWidth = 1.dp.toPx()
                        val path = Path().apply {
                            moveTo(0f, size.height - strokeWidth/2)
                            lineTo(size.width - strokeWidth/2, size.height - strokeWidth/2)
                            lineTo(size.width - strokeWidth/2, strokeWidth/2)
                            lineTo(strokeWidth/2, strokeWidth/2)
                            lineTo(strokeWidth/2, size.height - strokeWidth/2)
                        }
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = strokeWidth)
                        )
                    },
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B676B).copy(alpha = 1.0f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                            Text(
                                text = "السُنَّة",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE8D5B7),
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "عن نبينا محمد عليه أفضل الصلاة و السلام",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFB8A082).copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = sunnahText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF4E6D7),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                }
            }
        }
        
        // Main prayer card - positioned on top
    Card(
        modifier = Modifier
                .fillMaxWidth()
            .height(80.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .clickable { isExpanded = !isExpanded }
            .border(
                        width = 2.dp,
                color = Color.Black,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    ),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image
                Image(
                    painter = getPrayerBackground(prayerType),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Content overlay
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = arabicName,
                            fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                )
                Text(
                    text = englishName,
                            fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                )
            }
            Text(
                text = time,
                        fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerCardWithMargin(
    arabicName: String, 
    englishName: String, 
    time: String, 
    prayerType: String,
    expandedPrayer: String?,
    isAnimating: Boolean,
    onPrayerClick: (String?) -> Unit
) {
    val isExpanded = expandedPrayer == prayerType
    
    // Unified animation timeline - all animations use the same duration and easing
    val animationDuration = 300
    val animationSpec = tween<Float>(durationMillis = animationDuration, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    
    // Animation for the sliding box - starts behind prayer widget and slides down
    val slideOffset by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -1f,
        animationSpec = animationSpec,
        label = "drop_animation"
    )
    
    // Fade animation for smooth transition
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = animationSpec,
        label = "fade_animation"
    )
    
    // Animated margin - smooth transition when opening/closing
    val marginOffset by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = animationSpec,
        label = "margin_animation"
    )
    
    // Get specific sunnah text for each prayer
    val sunnahText = when (prayerType.lowercase()) {
        "fajr" -> "إثنتان قبل صلاة الفجر"
        "dhuhr" -> "أربع قبل الظهر [مثنى مثنى] تسليمتين و إثنتان بعد الظهر تسليمة واحدة"
        "asr" -> "لا يوجد"
        "maghrib" -> "إثنتان بعد المغرب"
        "isha" -> "إثنتان بعد العشاء"
        else -> "أربع قبل الظهر تسليمتين، وثنتان بعد الظهر تسليمة واحدة، وثنتان بعد المغرب، وثنتان بعد العشاء، وثنتان قبل صلاة الفجر،"
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Details box - positioned behind prayer widget, top sticks to prayer widget bottom
            if (isExpanded || alpha > 0f) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 80.dp - 8.dp + (slideOffset * 80).dp) // Adjust for rounded corners
                        .alpha(alpha)
                        .drawWithContent {
                            drawContent()
                            // Draw custom border without top edge
                            val strokeWidth = 1.dp.toPx()
                            val path = Path().apply {
                                moveTo(0f, size.height - strokeWidth/2)
                                lineTo(size.width - strokeWidth/2, size.height - strokeWidth/2)
                                lineTo(size.width - strokeWidth/2, strokeWidth/2)
                                lineTo(strokeWidth/2, strokeWidth/2)
                                lineTo(strokeWidth/2, size.height - strokeWidth/2)
                            }
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = strokeWidth)
                            )
                        },
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B676B).copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "السُنَّة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE8D5B7),
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "عن نبينا محمد عليه أفضل الصلاة و السلام",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFB8A082).copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = sunnahText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF4E6D7),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // Main prayer card - positioned on top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                    .clickable { 
                        onPrayerClick(if (isExpanded) null else prayerType)
                    }
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Background image
                    Image(
                        painter = getPrayerBackground(prayerType),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Content overlay
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = arabicName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black,
                                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                            Text(
                                text = englishName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black,
                                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                        Text(
                            text = time,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                }
            }
        }
        
        // Add proper bottom margin when expanded to give space for sunnah widget
        Spacer(
            modifier = Modifier.height((marginOffset * 80).dp) // Enough space for sunnah widget
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrayerTimesPreview() {
    Masjd2Theme {
        // Preview with mock data
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrayerCard("الفجر", "Fajr", "5:30 AM")
                PrayerCard("الظهر", "Dhuhr", "12:15 PM")
                PrayerCard("العصر", "Asr", "3:45 PM")
                PrayerCard("المغرب", "Maghrib", "6:20 PM")
                PrayerCard("العشاء", "Isha", "7:45 PM")
            }
        }
    }
}