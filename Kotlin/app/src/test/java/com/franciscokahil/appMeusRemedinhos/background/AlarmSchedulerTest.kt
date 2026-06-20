package com.franciscokahil.appMeusRemedinhos.background

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class AlarmSchedulerTest {

    private val context = mockk<Context>(relaxed = true)

    @Test
    fun `AlarmSchedulerImpl should not crash when AlarmManager is null`() {
        // Setup context to return null for AlarmManager
        every { context.getSystemService(Context.ALARM_SERVICE) } returns null
        
        val scheduler = AlarmSchedulerImpl(context)
        
        // These should not crash
        scheduler.scheduleAlarm("1", "Title", "Message", 8, 0)
        scheduler.cancelAlarm("1")
    }

    @Test
    fun `AlarmSchedulerImpl should not crash when AlarmManager throws exception`() {
        // Setup context to throw exception
        every { context.getSystemService(Context.ALARM_SERVICE) } throws RuntimeException("Service not found")
        
        val scheduler = AlarmSchedulerImpl(context)
        
        // These should not crash
        scheduler.scheduleAlarm("1", "Title", "Message", 8, 0)
        scheduler.cancelAlarm("1")
    }
}
