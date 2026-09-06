package com.borasarang.planjupjup

import com.borasarang.planjupjup.util.CrawlStats
import com.borasarang.planjupjup.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleLogicTest {

    @Test
    fun formatInterval_labelsMinutesHoursWeeks() {
        assertEquals("30분마다", TimeUtils.formatInterval(30))
        assertEquals("1시간마다", TimeUtils.formatInterval(60))
        assertEquals("6시간마다", TimeUtils.formatInterval(360))
        assertEquals("24시간마다", TimeUtils.formatInterval(1440))
        assertEquals("주 1회", TimeUtils.formatInterval(10080))
        assertEquals("미설정", TimeUtils.formatInterval(0))
    }

    @Test
    fun intervalOptions_coverExpectedChoices() {
        assertEquals(listOf(30, 60, 360, 720, 1440, 10080), TimeUtils.INTERVAL_OPTIONS_MINUTES)
    }

    @Test
    fun isFailureStreak_requiresFiveConsecutive() {
        assertTrue(CrawlStats.isFailureStreak(listOf("FAILED", "FAILED", "FAILED", "FAILED", "FAILED")))
        assertTrue(
            CrawlStats.isFailureStreak(
                listOf("FAILED", "FAILED", "FAILED", "FAILED", "FAILED", "SUCCESS"),
            ),
        )
        assertFalse(CrawlStats.isFailureStreak(listOf("FAILED", "FAILED", "FAILED", "FAILED")))
        assertFalse(
            CrawlStats.isFailureStreak(
                listOf("FAILED", "FAILED", "SUCCESS", "FAILED", "FAILED", "FAILED"),
            ),
        )
        assertFalse(CrawlStats.isFailureStreak(listOf("SUCCESS", "SUCCESS")))
        assertFalse(CrawlStats.isFailureStreak(emptyList()))
    }

    @Test
    fun startOfToday_returnsMidnightLocal() {
        val start = TimeUtils.startOfToday()
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(0, cal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(java.util.Calendar.MINUTE))
        assertEquals(0, cal.get(java.util.Calendar.SECOND))
        assertEquals(0, cal.get(java.util.Calendar.MILLISECOND))
        assertTrue(start <= System.currentTimeMillis())
    }

    @Test
    fun millisUntilNextHour_prefersTodayOrTomorrow() {
        val now = java.util.Calendar.getInstance()
        val delay = TimeUtils.millisUntilNextHour(9)
        assertTrue(delay in 0..TimeUtils.MILLIS_PER_DAY)

        val arrive = java.util.Calendar.getInstance().apply { timeInMillis = now.timeInMillis + delay }
        assertEquals(9, arrive.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(0, arrive.get(java.util.Calendar.MINUTE))

        val late = TimeUtils.millisUntilNextHour(now.get(java.util.Calendar.HOUR_OF_DAY))
        assertTrue(late > 0)
    }
}
