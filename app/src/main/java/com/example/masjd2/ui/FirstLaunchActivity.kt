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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import android.content.IntentSender
import kotlinx.coroutines.launch

/**
 * First launch activity for requesting permissions and setting up prayer times
 */
class FirstLaunchActivity : ComponentActivity() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prayerRepository: PrayerRepository
    private lateinit var settingsClient: SettingsClient
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted
                getCurrentLocation()
            }
            else -> {
                // No location access granted
                Toast.makeText(this, "Location permission is required for prayer times", Toast.LENGTH_LONG).show()
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
            FirstLaunchScreen(
                prayerRepository = prayerRepository,
                onRequestPermissions = { requestLocationPermissions() },
                onCheckInternet = { checkInternetConnection() }
            )
        }
    }
    
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            when (resultCode) {
                RESULT_OK -> {
                    // Location settings were enabled, try to get location again
                    getLocationWithTimeout()
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
    
    private fun requestLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                getCurrentLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Show explanation
                Toast.makeText(this, "Location permission is needed to get accurate prayer times", Toast.LENGTH_LONG).show()
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                // Request permission
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun getCurrentLocation() {
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
        checkLocationSettings()
    }
    
    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000 // 10 seconds
        ).build()
        
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        
        task.addOnSuccessListener {
            // Location settings are satisfied, get location
            getLocationWithTimeout()
        }
        
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult()
                    exception.startResolutionForResult(this, 1001)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                    Toast.makeText(this, "Please enable location services in settings", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Location services are not available. Please enable them in settings.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun getLocationWithTimeout() {
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
                    setupPrayerTimes(location.latitude, location.longitude)
                } else {
                    // If no last known location, try fresh location with timeout
                    requestFreshLocationWithTimeout()
                }
            }
            .addOnFailureListener {
                // If last location fails, try fresh location
                requestFreshLocationWithTimeout()
            }
    }
    
    private fun requestFreshLocationWithTimeout() {
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
                setupPrayerTimes(location.latitude, location.longitude)
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
    
    private fun setupPrayerTimes(latitude: Double, longitude: Double) {
        // Check internet connection
        if (!checkInternetConnection()) {
            Toast.makeText(this, "Internet connection is required for first setup", Toast.LENGTH_LONG).show()
            return
        }
        
        // Get location info
        lifecycleScope.launch {
            try {
                val (country, city) = prayerRepository.getLocationInfo(latitude, longitude)
                
                // Get calculation method
                val method = prayerRepository.getCalculationMethodForCountry(country)
                
                // Navigate to download progress activity
                val intent = Intent(this@FirstLaunchActivity, DownloadProgressActivity::class.java).apply {
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                    putExtra("method", method)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@FirstLaunchActivity, "Setup failed: ${e.message}", Toast.LENGTH_LONG).show()
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
fun FirstLaunchScreen(
    prayerRepository: PrayerRepository,
    onRequestPermissions: () -> Unit,
    onCheckInternet: () -> Boolean
) {
    var showInternetDialog by remember { mutableStateOf(false) }
    var isLocationRequested by remember { mutableStateOf(false) }
    var isFirstTime by remember { mutableStateOf(true) }

    // Check if this is first time or need to download new year
    LaunchedEffect(Unit) {
        val hasAnyPrayerTimes = prayerRepository.hasAnyPrayerTimes()
        isFirstTime = !hasAnyPrayerTimes
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
        Text(
            text = if (isFirstTime) "Welcome to Prayer Times" else "Update Prayer Times",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isFirstTime) 
                "To provide accurate prayer times, we need to:" 
            else 
                "Your prayer times are outdated. We need to download the new year:",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
                    text = "1. Access your location",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "We'll use your location to calculate accurate prayer times",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "2. Connect to internet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "We'll download prayer times for the entire year",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "3. Auto-select calculation method",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "Based on your country (Egypt → Egyptian, Saudi Arabia → Umm al-Qura, etc.)",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "⚠️ Make sure location services are enabled on your device",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
                Button(
                    onClick = {
                        if (onCheckInternet()) {
                            isLocationRequested = true
                            onRequestPermissions()
                        } else {
                            showInternetDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocationRequested,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF20424f)
                    )
                ) {
                    if (isLocationRequested) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Getting Location...", color = Color.White)
                    } else {
                        Text(
                            text = if (isFirstTime) "Grant Location Permission" else "Update Prayer Times",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
        }
    }
    
    // Internet required dialog
    if (showInternetDialog) {
        AlertDialog(
            onDismissRequest = { showInternetDialog = false },
            title = { Text("Internet Required") },
            text = { Text("Internet connection is required for first setup. Please check your connection and try again.") },
            confirmButton = {
                TextButton(onClick = { showInternetDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
