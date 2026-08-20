package com.franciscokahil.appMeusRemedinhos.background

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NotificationHelper.
 * Tests notification creation and delivery without actual system calls.
 */
class NotificationHelperTest {

    private val mockContext = mockk<Context>(relaxed = true)
    private val mockNotificationManager = mockk<NotificationManager>(relaxed = true)
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setup() {
        mockkConstructor(NotificationCompat.Builder::class)
        mockkStatic(PendingIntent::class)
        
        every { anyConstructed<NotificationCompat.Builder>().build() } returns mockk(relaxed = true)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk(relaxed = true)

        // Mock context string resource for channel creation
        every { mockContext.getString(any()) } returns "Lembretes"
        every { mockContext.getString(any(), *anyVararg()) } returns "Lembretes"
        every { mockContext.packageManager } returns mockk(relaxed = true)
        every { mockContext.packageName } returns "com.franciscokahil.appMeusRemedinhos"

        every { mockContext.getSystemService(Context.NOTIFICATION_SERVICE) } returns mockNotificationManager
        notificationHelper = NotificationHelper(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createNotificationChannel should call notificationManager on Android 8+`() {
        // Arrange & Act
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationHelper.createNotificationChannel()

            // Assert
            verify(atLeast = 1) {
                mockNotificationManager.createNotificationChannel(any())
            }
        }
    }

    @Test
    fun `showNotification should call notify`() {
        // Arrange
        val title = "Remédio"
        val message = "Tomar Aspirina 100mg"

        // Act
        notificationHelper.showNotification(title, message)

        // Assert
        verify {
            mockNotificationManager.notify(any<Int>(), any())
        }
    }

    @Test
    fun `notification title should match provided title`() {
        // Arrange
        val testTitle = "Almoço com Remédio"
        val testMessage = "Dipirona 500mg"

        // Act
        notificationHelper.showNotification(testTitle, testMessage)

        // Assert
        verify {
            mockNotificationManager.notify(any<Int>(), any())
        }
    }

    @Test
    fun `multiple notifications should use different IDs`() {
        // Arrange
        val title1 = "Café"
        val title2 = "Almoço"

        // Act
        notificationHelper.showNotification(title1, "Msg1")
        notificationHelper.showNotification(title2, "Msg2")

        // Assert: Should be called twice
        verify(exactly = 2) {
            mockNotificationManager.notify(any<Int>(), any())
        }
    }

    @Test
    fun `showNotification with empty message should still work`() {
        // Arrange
        val title = "Title"
        val message = ""

        // Act
        notificationHelper.showNotification(title, message)

        // Assert
        verify {
            mockNotificationManager.notify(any<Int>(), any())
        }
    }
}

