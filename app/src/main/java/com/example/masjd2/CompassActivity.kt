package com.example.masjd2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.masjd2.ui.theme.Masjd2Theme
import com.example.masjd2.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.*

class CompassActivity : ComponentActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var rotationVectorSensor: Sensor? = null
    private var currentAzimuth: Float = 0f
    private var qiblaDirection: Float = 0f
    private var userLocation: Location? = null
    
    // State variables for real-time updates
    private var isLocationReady = false
    private var isSensorReady = false
    
    // Mutable state for Compose
    private var _currentAzimuth by mutableStateOf(0f)
    private var _qiblaDirection by mutableStateOf(0f)
    private var _isLocationReady by mutableStateOf(false)
    private var _isSensorReady by mutableStateOf(false)
    
    // Qibla coordinates (Kaaba in Mecca)
    private val qiblaLatitude = 21.4225
    private val qiblaLongitude = 39.8262
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Initialize sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is required to use the compass.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Get user location
        getCurrentLocation()
        
        setContent {
            Masjd2Theme {
                CompassScreen(
                    currentAzimuth = _currentAzimuth,
                    qiblaDirection = _qiblaDirection,
                    userLocation = userLocation,
                    isLocationReady = _isLocationReady,
                    isSensorReady = _isSensorReady
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        rotationVectorSensor?.let { sensor ->
            // Use balanced sensor updates for stable needle movement
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("CompassActivity", "Sensor listener registered")
        } ?: run {
            Log.e("CompassActivity", "Rotation vector sensor not available")
        }
    }
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = it
                    qiblaDirection = calculateQiblaDirection(it.latitude, it.longitude)
                    
                    // Update mutable state for Compose
                    _qiblaDirection = qiblaDirection
                    _isLocationReady = true
                    isLocationReady = true
                    
                    Log.d("CompassActivity", "Location: ${it.latitude}, ${it.longitude}")
                    Log.d("CompassActivity", "Qibla direction: $qiblaDirection degrees")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CompassActivity", "Error getting location: ${exception.message}")
                Toast.makeText(this, "Unable to get location. Using default Qibla direction.", Toast.LENGTH_LONG).show()
                // Use a default direction (e.g., East) if location fails
                qiblaDirection = 90f
            }
    }
    
    private fun calculateQiblaDirection(latitude: Double, longitude: Double): Float {
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(qiblaLatitude)
        val deltaLon = Math.toRadians(qiblaLongitude - longitude)
        
        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
        
        var bearing = Math.toDegrees(atan2(y, x))
        if (bearing < 0) bearing += 360
        
        Log.d("CompassActivity", "User location: $latitude, $longitude")
        Log.d("CompassActivity", "Qibla location: $qiblaLatitude, $qiblaLongitude")
        Log.d("CompassActivity", "Calculated Qibla bearing: $bearing degrees")
        
        return bearing.toFloat()
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            
            // Convert from radians to degrees and normalize to 0-360
            val newAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val normalizedAzimuth = if (newAzimuth < 0) newAzimuth + 360 else newAzimuth
            
            // Only update if the change is significant to avoid jitter
            if (kotlin.math.abs(normalizedAzimuth - currentAzimuth) > 1f) {
                currentAzimuth = normalizedAzimuth
                _currentAzimuth = currentAzimuth
                _isSensorReady = true
                isSensorReady = true
                
                Log.d("CompassActivity", "Device azimuth: $currentAzimuth degrees")
                Log.d("CompassActivity", "Qibla direction: $qiblaDirection degrees")
                Log.d("CompassActivity", "Needle should point to: ${(qiblaDirection - currentAzimuth + 360) % 360} degrees")
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}

@Composable
fun CompassScreen(
    currentAzimuth: Float,
    qiblaDirection: Float,
    userLocation: Location?,
    isLocationReady: Boolean,
    isSensorReady: Boolean
) {
    // Calculate the angle the needle should point to Qibla
    // The needle should point to the Qibla direction relative to the device's current orientation
    val needleRotation = (qiblaDirection - currentAzimuth + 360) % 360
    
    // Smooth needle rotation with 300ms animation for better user experience
    val animatedRotation by animateFloatAsState(
        targetValue = needleRotation,
        animationSpec = tween(durationMillis = 300), // Faster response for better accuracy
        label = "needle_rotation"
    )
    
    // Force recomposition for real-time updates when sensor values change
    LaunchedEffect(currentAzimuth, qiblaDirection) {
        // This ensures the screen updates when sensor values change
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B2951))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Qibla Compass",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Compass container
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        color = Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Compass background
                Image(
                    painter = painterResource(id = R.drawable.compass_bg),
                    contentDescription = "Compass Background",
                    modifier = Modifier.size(300.dp)
                )
                
                // Qibla needle
                Image(
                    painter = painterResource(id = R.drawable.needle_red),
                    contentDescription = "Qibla Needle",
                    modifier = Modifier
                        .size(200.dp)
                        .rotate(animatedRotation)
                )
                
                // Kaaba emoji at the center
                Text(
                    text = "🕋",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Information text
            if (isLocationReady && isSensorReady) {
                Text(
                    text = "The red needle points to Qibla 🕋 - rotate your device to face the needle",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Initializing compass...",
                    fontSize = 16.sp,
                    color = Color(0xFFB8A082),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Direction information
            Text(
                text = "Qibla Direction: ${qiblaDirection.toInt()}°",
                fontSize = 14.sp,
                color = Color(0xFFB8A082),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Device Direction: ${currentAzimuth.toInt()}°",
                fontSize = 14.sp,
                color = Color(0xFFB8A082),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Needle Points: ${animatedRotation.toInt()}°",
                fontSize = 14.sp,
                color = Color(0xFFB8A082),
                textAlign = TextAlign.Center
            )
            
            userLocation?.let { location ->
                Text(
                    text = "Your Location: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}",
                    fontSize = 12.sp,
                    color = Color(0xFFB8A082),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
