package com.example.masjd2

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class QiblaDirectionTest {

    private fun calculateQiblaDirection(latitude: Double, longitude: Double): Float {
        val qiblaLatitude = 21.4225
        val qiblaLongitude = 39.8262

        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(qiblaLatitude)
        val deltaLon = Math.toRadians(qiblaLongitude - longitude)

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        var bearing = Math.toDegrees(atan2(y, x))
        if (bearing < 0) bearing += 360

        return bearing.toFloat()
    }

    @Test
    fun qiblaDirectionFromCairo_isCorrect() {
        // Cairo, Egypt - should roughly face ~136° (SE)
        val direction = calculateQiblaDirection(30.0444, 31.2357)
        assertEquals(136.0, direction.toDouble(), 1.0)
    }

    @Test
    fun qiblaDirectionFromMecca_isZero() {
        // Mecca itself - direction should be 0 or 360
        val direction = calculateQiblaDirection(21.4225, 39.8262)
        assertTrue(direction < 0.1 || direction > 359.9)
    }

    @Test
    fun qiblaDirectionFromNorthPole_isValid() {
        // From the North Pole, the great circle route to Kaaba (21.4225°N, 39.8262°E)
        // follows a bearing of approximately 140°.
        val direction = calculateQiblaDirection(90.0, 0.0)
        assertEquals("Bearing from North Pole to Kaaba", 140.0, direction.toDouble(), 1.0)
    }

    @Test
    fun qiblaDirectionIsAlwaysInZeroTo360() {
        for (lat in listOf(-90.0, -45.0, 0.0, 45.0, 90.0)) {
            for (lon in listOf(-180.0, -90.0, 0.0, 90.0, 180.0)) {
                val direction = calculateQiblaDirection(lat, lon)
                assertTrue("Direction $direction out of range for ($lat, $lon)", direction in 0.0..360.0)
            }
        }
    }
}
