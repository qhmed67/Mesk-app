package com.example.masjd2.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.masjd2.MainActivity
import com.example.masjd2.R
import com.example.masjd2.repository.PrayerRepository
import com.example.masjd2.services.AthanAlarmManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import android.content.IntentSender
import kotlinx.coroutines.launch

/**
 * Settings activity with reload prayer times functionality
 */
class SettingsActivity : ComponentActivity() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prayerRepository: PrayerRepository
    private lateinit var settingsClient: SettingsClient
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocationForReload()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocationForReload()
            }
            else -> {
                Toast.makeText(this, "Location permission is required to reload prayer times", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)
        prayerRepository = PrayerRepository(this)
        
        setContent {
            SettingsScreen(
                prayerRepository = prayerRepository,
                onBackPressed = { finish() },
                onReloadPrayerTimes = { requestLocationPermissionsForReload() }
            )
        }
    }
    
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002) {
            when (resultCode) {
                RESULT_OK -> {
                    // Location settings were enabled, try to get location again
                    getLocationWithTimeoutForReload()
                }
                RESULT_CANCELED -> {
                    // User cancelled location services - stop loading and show message
                    Toast.makeText(this, "Location services are required for prayer times. Please enable them in settings and try again.", Toast.LENGTH_LONG).show()
                    // Reset the loading state by finishing and restarting the activity
                    finish()
                    startActivity(intent)
                }
            }
        }
    }
    
    private fun requestLocationPermissionsForReload() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocationForReload()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Toast.makeText(this, "Location permission is needed to reload prayer times", Toast.LENGTH_LONG).show()
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun getCurrentLocationForReload() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        // First check if location services are enabled
        checkLocationSettingsForReload()
    }
    
    private fun checkLocationSettingsForReload() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000 // 10 seconds
        ).build()
        
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        
        val task = settingsClient.checkLocationSettings(builder.build())
        
        task.addOnSuccessListener {
            // Location settings are satisfied, get location
            getLocationWithTimeoutForReload()
        }
        
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult()
                    exception.startResolutionForResult(this, 1002)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                    Toast.makeText(this, "Please enable location services in settings", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Location services are not available. Please enable them in settings.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun getLocationWithTimeoutForReload() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        // First try last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    reloadPrayerTimes(location.latitude, location.longitude)
                } else {
                    // If no last known location, try fresh location with timeout
                    requestFreshLocationWithTimeoutForReload()
                }
            }
            .addOnFailureListener {
                // If last location fails, try fresh location
                requestFreshLocationWithTimeoutForReload()
            }
    }
    
    private fun requestFreshLocationWithTimeoutForReload() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        val cancellationTokenSource = CancellationTokenSource()
        
        // Set a timeout of 15 seconds
        lifecycleScope.launch {
            kotlinx.coroutines.delay(15000)
            cancellationTokenSource.cancel()
        }
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                reloadPrayerTimes(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Unable to get current location. Please try again or check your location settings.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            if (exception.message?.contains("cancelled") == true) {
                Toast.makeText(this, "Location request timed out. Please ensure location services are enabled and try again.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to get location: ${exception.message}. Please check your location settings.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun reloadPrayerTimes(latitude: Double, longitude: Double) {
        // Check internet connection
        if (!checkInternetConnection()) {
            Toast.makeText(this, "Internet connection is required to reload prayer times", Toast.LENGTH_LONG).show()
            return
        }
        
        // Get location info and method
        lifecycleScope.launch {
            try {
                val (country, city) = prayerRepository.getLocationInfo(latitude, longitude)
                val method = prayerRepository.getCalculationMethodForCountry(country)
                
                // Navigate to download progress activity for reload
                val intent = Intent(this@SettingsActivity, DownloadProgressActivity::class.java).apply {
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                    putExtra("method", method)
                    putExtra("isReload", true) // Flag to indicate this is a reload
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Reload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun checkInternetConnection(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@Composable
fun SettingsScreen(
    prayerRepository: PrayerRepository,
    onBackPressed: () -> Unit,
    onReloadPrayerTimes: () -> Unit
) {
    val context = LocalContext.current
    var showReloadDialog by remember { mutableStateOf(false) }
    
    // Athan settings state
    val athanSettings by prayerRepository.getAthanSettingsFlow().collectAsState(initial = null)
    var isAthanEnabled by remember { mutableStateOf(true) }
    var athanVolume by remember { mutableStateOf(1.0f) }
    var customAthanPath by remember { mutableStateOf<String?>(null) }
    
    // Update local state when settings change
    LaunchedEffect(athanSettings) {
        athanSettings?.let { settings ->
            isAthanEnabled = settings.isAthanEnabled
            athanVolume = settings.athanVolume
            customAthanPath = settings.customAthanPath
        }
    }
    
    // File picker for MP3 files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Copy the file to internal storage and save path
            val fileName = "custom_athan_${System.currentTimeMillis()}.mp3"
            val internalFile = java.io.File(context.filesDir, fileName)
            
            try {
                context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                    internalFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                // Update the custom Athan path
                customAthanPath = internalFile.absolutePath
                
                // Save to repository
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    prayerRepository.updateCustomAthanPath(internalFile.absolutePath)
                }
                
                android.widget.Toast.makeText(context, "Custom Athan audio selected!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error selecting file: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Athan Settings Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF20424f).copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🕌 Athan Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enable/Disable Athan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Athan Alarms",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "Play Athan at prayer times",
                            fontSize = 14.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                    Switch(
                        checked = isAthanEnabled,
                        onCheckedChange = { enabled ->
                            isAthanEnabled = enabled
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                prayerRepository.updateAthanEnabled(enabled)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50), // Green when ON
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFF44336) // Red when OFF
                        )
                    )
                }
                
                if (isAthanEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Volume Control
                    Column {
                        Text(
                            text = "Athan Volume",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "Adjust Athan playback volume (${(athanVolume * 100).toInt()}%)",
                            fontSize = 14.sp,
                            color = Color(0xFFB0BEC5)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = athanVolume,
                            onValueChange = { volume ->
                                athanVolume = volume
                            },
                            onValueChangeFinished = {
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    prayerRepository.updateAthanVolume(athanVolume)
                                }
                            },
                            valueRange = 0.1f..1.0f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sound Selection
                    Column {
                        Text(
                            text = "Athan Sound",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = if (customAthanPath != null) "Custom MP3 selected" else "Default alarm sound",
                            fontSize = 14.sp,
                            color = Color(0xFFB0BEC5)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Upload Custom MP3 Button
                            OutlinedButton(
                                onClick = {
                                    filePickerLauncher.launch("audio/mpeg")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Upload MP3", color = Color.White)
                            }
                            
                            // Reset to Default Button
                            if (customAthanPath != null) {
                                OutlinedButton(
                                    onClick = {
                                        customAthanPath = null
                                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                            prayerRepository.updateCustomAthanPath(null)
                                        }
                                        android.widget.Toast.makeText(context, "Reset to default sound", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Use Default", color = Color.White)
                                }
                            }
                        }
                        
                        // Permissions Button
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = android.content.Intent(context, PermissionsActivity::class.java).apply {
                                    putExtra(PermissionsActivity.EXTRA_FROM_SETTINGS, true)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⚙️ Manage Athan Permissions", color = Color.White)
                        }


                        // Info about MP3 files and controls
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "💡 How to stop Athan:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• Press Volume Down key",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• Tap 'Stop Athan' in notification",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📁 Only MP3 files are supported. Athan plays until manually stopped.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reload Prayer Times Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF20424f).copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Reload Prayer Times",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Get fresh prayer times for your current location. This will:",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Detect your current location",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "• Auto-select calculation method based on country",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "• Download prayer times for the entire year",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "• Update your location and method preferences",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "⚠️ Make sure location services are enabled on your device",
                    fontSize = 14.sp,
                    color = Color(0xFFFFB74D),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showReloadDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Reload Prayer Times",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF20424f).copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About Prayer Times",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Prayer times are calculated using the AlAdhan API and stored locally on your device. The calculation method is automatically selected based on your country:",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Egypt → Egyptian General Authority",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "• Saudi Arabia → Umm al-Qura",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
                Text(
                    text = "• Other countries → Muslim World League",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }
    }
    
    // Reload confirmation dialog
    if (showReloadDialog) {
        AlertDialog(
            onDismissRequest = { showReloadDialog = false },
            title = { Text("Reload Prayer Times") },
            text = { 
                Text("This will clear your current prayer times and download fresh data for your current location. All Athan alarms will be rescheduled with the new prayer times. This requires internet connection and location permission. Continue?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReloadDialog = false
                        onReloadPrayerTimes()
                    }
                ) {
                    Text("Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReloadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
}
