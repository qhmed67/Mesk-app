package com.example.masjd2

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class PrayerTimeTest {

    private fun convertTo24Hour(time12: String): String {
        val upper = time12.uppercase().trim()
        val isPM = upper.contains("PM")
        val timePart = upper.replace("AM", "").replace("PM", "").trim()
        val parts = timePart.split(":")
        var hour = parts[0].toInt()
        val minute = parts[1]
        if (isPM && hour != 12) hour += 12
        if (!isPM && hour == 12) hour = 0
        return "${hour.toString().padStart(2, '0')}:$minute"
    }

    @Test
    fun convertTo24Hour_worksForAllFormats() {
        assertEquals("05:30", convertTo24Hour("5:30 AM"))
        assertEquals("12:00", convertTo24Hour("12:00 PM"))
        assertEquals("00:00", convertTo24Hour("12:00 AM"))
        assertEquals("15:45", convertTo24Hour("3:45 PM"))
        assertEquals("23:59", convertTo24Hour("11:59 PM"))
    }

    @Test
    fun utcTimestampCairoJanIsUtcPlus2or3() {
        val tz = ZoneId.of("Africa/Cairo")
        val offset = tz.getRules().getOffset(LocalDateTime.of(2025, 1, 1, 5, 30))
        assertTrue("Cairo Jan offset: $offset", offset in listOf(ZoneOffset.ofHours(2), ZoneOffset.ofHours(3)))
    }

    @Test
    fun utcTimestampNewYorkShiftsByOneHourAcrossDST() {
        val tz = ZoneId.of("America/New_York")
        
        // 2025 DST: starts March 9 (2nd Sunday)
        // Mar 8 EST (UTC-5), Mar 10 EDT (UTC-4)
        val offsetMar8 = tz.getRules().getOffset(LocalDateTime.of(2025, 3, 8, 5, 30))
        val offsetMar10 = tz.getRules().getOffset(LocalDateTime.of(2025, 3, 10, 5, 30))
        
        assertEquals("EST offset", ZoneOffset.ofHours(-5), offsetMar8)
        assertEquals("EDT offset", ZoneOffset.ofHours(-4), offsetMar10)
    }

    @Test
    fun prayerOrdering_isCorrect() {
        val now = 100000L
        val prayers = listOf(
            "fajr" to 50000L,
            "dhuhr" to 150000L,
            "asr" to 200000L,
            "maghrib" to 300000L,
            "isha" to 400000L
        )

        val next = prayers.firstOrNull { (_, utc) -> utc > now }
        assertNotNull(next)
        assertEquals("dhuhr", next!!.first)
    }

    @Test
    fun allPrayersPassed_returnsNone() {
        val now = 500000L
        val prayers = listOf(
            "fajr" to 100000L,
            "dhuhr" to 200000L,
            "asr" to 300000L,
            "maghrib" to 400000L,
            "isha" to 500000L
        )

        val next = prayers.firstOrNull { (_, utc) -> utc > now }
        assertNull(next)
    }
}
