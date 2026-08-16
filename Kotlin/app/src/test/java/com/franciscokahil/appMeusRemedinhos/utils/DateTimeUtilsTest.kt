package com.franciscokahil.appMeusRemedinhos.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateTimeUtilsTest {

    @Test
    fun `isTimePassed should return true for past time`() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        val eventTime = "08:00"
        assertTrue(DateTimeUtils.isTimePassed(eventTime, now))
    }

    @Test
    fun `isTimePassed should return false for future time`() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        val eventTime = "12:00"
        assertFalse(DateTimeUtils.isTimePassed(eventTime, now))
    }

    @Test
    fun `isTimePassed should return false for same time`() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val eventTime = "10:00"
        assertFalse(DateTimeUtils.isTimePassed(eventTime, now))
    }

    @Test
    fun `isTimePassed should handle invalid format`() {
        val now = Calendar.getInstance()
        assertFalse(DateTimeUtils.isTimePassed("invalid", now))
        assertFalse(DateTimeUtils.isTimePassed("10:xx", now))
    }
}
