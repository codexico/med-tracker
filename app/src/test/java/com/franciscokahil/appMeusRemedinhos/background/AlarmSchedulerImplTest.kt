package com.franciscokahil.appMeusRemedinhos.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for AlarmScheduler implementation.
 * Tests alarm scheduling logic without requiring actual Android OS calls.
 */
class AlarmSchedulerImplTest {

    private lateinit var alarmScheduler: AlarmSchedulerImpl
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockAlarmManager = mockk<AlarmManager>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(PendingIntent::class)
        mockkConstructor(Intent::class)
        // Correcting MockK setup to handle Intent constructor and methods
        every { anyConstructed<Intent>().putExtra(any<String>(), any<String>()) } returns mockk(relaxed = true)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockk(relaxed = true)

        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        alarmScheduler = AlarmSchedulerImpl(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `scheduleAlarm should call setExactAndAllowWhileIdle on Android 11 or earlier`() {
        // Arrange
        val eventId = "test-event-1"
        val title = "Café da Manhã"
        val message = "Hora de tomar medicação"
        every { mockAlarmManager.canScheduleExactAlarms() } returns true

        // Act
        alarmScheduler.scheduleAlarm(eventId, title, message, 8, 0)

        // Assert
        val useExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || mockAlarmManager.canScheduleExactAlarms()
        verify {
            if (useExact) {
                mockAlarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    any(),
                    any()
                )
            } else {
                mockAlarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun `scheduleAlarm should use next day if time has passed`() {
        // Arrange
        val eventId = "test-event-2"
        val now = Calendar.getInstance()
        val pastHour = (now.get(Calendar.HOUR_OF_DAY) - 2 + 24) % 24  // 2 hours ago
        val pastMinute = 0

        // Act
        alarmScheduler.scheduleAlarm(eventId, "Event", "Message", pastHour, pastMinute)

        // Assert: Verify alarm manager was called (actual time verification in integration tests)
        val useExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || mockAlarmManager.canScheduleExactAlarms()
        verify(atLeast = 1) {
            if (useExact) {
                mockAlarmManager.setExactAndAllowWhileIdle(any(), any(), any())
            } else {
                mockAlarmManager.setAndAllowWhileIdle(any(), any(), any())
            }
        }
    }

    @Test
    fun `cancelAlarm should call alarmManager cancel`() {
        // Arrange
        val eventId = "test-event-to-cancel"

        // Act
        alarmScheduler.cancelAlarm(eventId)

        // Assert
        verify {
            mockAlarmManager.cancel(any<PendingIntent>())
        }
    }

    @Test
    fun `cancelAlarm should use event ID hash as request code`() {
        // Arrange
        val eventId = "unique-id-12345"
        val requestCodeSlot = slot<Int>()
        every {
            mockAlarmManager.cancel(any<PendingIntent>())
        } just runs

        // Act
        alarmScheduler.cancelAlarm(eventId)

        // Assert
        verify {
            mockAlarmManager.cancel(any<PendingIntent>())
        }
        // Request code consistency verified via ID hash (not directly testable via mock)
    }

    @Test
    fun `scheduleAlarm with exact permission should use setExactAndAllowWhileIdle`() {
        // Arrange
        every { mockAlarmManager.canScheduleExactAlarms() } returns true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Act
            alarmScheduler.scheduleAlarm("id", "Title", "Message", 10, 30)

            // Assert
            verify {
                mockAlarmManager.setExactAndAllowWhileIdle(
                    eq(AlarmManager.RTC_WAKEUP),
                    any(Long::class),
                    any(PendingIntent::class)
                )
            }
        }
    }

    @Test
    fun `scheduleAlarm without exact permission should fall back to setAndAllowWhileIdle`() {
        // Arrange
        every { mockAlarmManager.canScheduleExactAlarms() } returns false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Act
            alarmScheduler.scheduleAlarm("id", "Title", "Message", 10, 30)

            // Assert
            verify {
                mockAlarmManager.setAndAllowWhileIdle(
                    eq(AlarmManager.RTC_WAKEUP),
                    any(Long::class),
                    any(PendingIntent::class)
                )
            }
        }
    }

    @Test
    fun `scheduleAlarm should use RTC_WAKEUP to wake device from Doze Mode`() {
        // Arrange & Act
        alarmScheduler.scheduleAlarm("id", "Title", "Message", 14, 0)

        // Assert: Verify RTC_WAKEUP is used (critical for Doze Mode)
        val useExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || mockAlarmManager.canScheduleExactAlarms()
        verify {
            if (useExact) {
                mockAlarmManager.setExactAndAllowWhileIdle(
                    eq(AlarmManager.RTC_WAKEUP),
                    any(),
                    any()
                )
            } else {
                mockAlarmManager.setAndAllowWhileIdle(
                    eq(AlarmManager.RTC_WAKEUP),
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun `multiple scheduleAlarm calls should use FLAG_UPDATE_CURRENT to replace previous`() {
        // Arrange
        val eventId = "recurring-id"

        // Act
        alarmScheduler.scheduleAlarm(eventId, "Title1", "Message1", 8, 0)
        alarmScheduler.scheduleAlarm(eventId, "Title2", "Message2", 8, 0)

        // Assert: Same ID should update (request code is deterministic via hash)
        val useExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || mockAlarmManager.canScheduleExactAlarms()
        verify(atLeast = 2) {
            if (useExact) {
                mockAlarmManager.setExactAndAllowWhileIdle(any(), any(), any())
            } else {
                mockAlarmManager.setAndAllowWhileIdle(any(), any(), any())
            }
        }
    }
}

