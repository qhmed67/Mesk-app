package com.example.masjd2

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.masjd2.ui.theme.Masjd2Theme
import kotlin.math.abs
import kotlin.math.sqrt

class CompassCalibrationActivity : ComponentActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    
    // Motion detection variables
    private var lastAccelerometerValues = FloatArray(3)
    private var lastMagnetometerValues = FloatArray(3)
    private var motionStartTime = 0L
    private var motionDetected = false
    private var calibrationComplete = false
    
    // Motion thresholds
    private val motionThreshold = 2.0f // Minimum acceleration change
    private val calibrationDuration = 3000L // 3 seconds of motion required
    private val requiredMotionChanges = 10 // Minimum number of significant changes
    
    private var motionChangeCount = 0
    private var lastMotionTime = 0L
    
    // State for UI
    private var _statusText by mutableStateOf("Waiting for movement...")
    private var _isCalibrated by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        setContent {
            Masjd2Theme {
                CompassCalibrationScreen(
                    statusText = _statusText,
                    isCalibrated = _isCalibrated,
                    onCalibrationComplete = {
                        navigateToCompass()
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        startMotionDetection()
    }
    
    override fun onPause() {
        super.onPause()
        stopMotionDetection()
    }
    
    private fun startMotionDetection() {
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        Log.d("CompassCalibration", "Motion detection started")
    }
    
    private fun stopMotionDetection() {
        sensorManager.unregisterListener(this)
        Log.d("CompassCalibration", "Motion detection stopped")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (calibrationComplete) return
        
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    detectMotion(sensorEvent.values)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    // Store magnetometer values for potential future use
                    lastMagnetometerValues = sensorEvent.values.clone()
                }
            }
        }
    }
    
    private fun detectMotion(accelerometerValues: FloatArray) {
        val currentTime = System.currentTimeMillis()
        
        // Calculate acceleration magnitude
        val currentMagnitude = sqrt(
            accelerometerValues[0] * accelerometerValues[0] +
            accelerometerValues[1] * accelerometerValues[1] +
            accelerometerValues[2] * accelerometerValues[2]
        )
        
        val lastMagnitude = sqrt(
            lastAccelerometerValues[0] * lastAccelerometerValues[0] +
            lastAccelerometerValues[1] * lastAccelerometerValues[1] +
            lastAccelerometerValues[2] * lastAccelerometerValues[2]
        )
        
        // Check for significant motion
        val accelerationChange = abs(currentMagnitude - lastMagnitude)
        
        if (accelerationChange > motionThreshold) {
            if (!motionDetected) {
                motionDetected = true
                motionStartTime = currentTime
                motionChangeCount = 0
                Log.d("CompassCalibration", "Motion detected, starting calibration timer")
            }
            
            motionChangeCount++
            lastMotionTime = currentTime
            
            // Check if we have enough motion changes and duration
            val motionDuration = currentTime - motionStartTime
            if (motionChangeCount >= requiredMotionChanges && motionDuration >= calibrationDuration) {
                completeCalibration()
            }
        } else {
            // If no motion for too long, reset
            if (motionDetected && (currentTime - lastMotionTime) > 2000) {
                resetMotionDetection()
            }
        }
        
        // Update last values
        lastAccelerometerValues = accelerometerValues.clone()
    }
    
    private fun resetMotionDetection() {
        motionDetected = false
        motionChangeCount = 0
        _statusText = "Waiting for movement..."
        Log.d("CompassCalibration", "Motion detection reset")
    }
    
    private fun completeCalibration() {
        if (calibrationComplete) return
        
        calibrationComplete = true
        _isCalibrated = true
        _statusText = "Compass calibrated successfully!"
        
        Log.d("CompassCalibration", "Calibration completed successfully")
        
        // Stop sensors
        stopMotionDetection()
        
        // Navigate to compass after a short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            navigateToCompass()
        }, 1500)
    }
    
    private fun navigateToCompass() {
        val intent = Intent(this, CompassActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}

@Composable
fun CompassCalibrationScreen(
    statusText: String,
    isCalibrated: Boolean,
    onCalibrationComplete: () -> Unit
) {
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
            // Lottie Animation
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("loading_eight.json"))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
            
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp)
            )
            
            // Instruction text
            Text(
                text = "Move your phone in the same direction as the motion above",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Status text
            Text(
                text = statusText,
                fontSize = 14.sp,
                color = if (isCalibrated) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontWeight = if (isCalibrated) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
