package com.franciscokahil.appMeusRemedinhos.background

import android.app.NotificationManager
import android.content.Context
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        notificationManager = mock(NotificationManager::class.java)
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager)
        notificationHelper = NotificationHelper(context)
    }

    @Test
    fun getStockNotificationId_returnsUniqueIdPerMedication() {
        val idMed1 = notificationHelper.getStockNotificationId(1L)
        val idMed2 = notificationHelper.getStockNotificationId(2L)

        assertNotEquals(idMed1, idMed2)
        assertEquals(NotificationHelper.STOCK_NOTIFICATION_BASE_ID + 1, idMed1)
        assertEquals(NotificationHelper.STOCK_NOTIFICATION_BASE_ID + 2, idMed2)
    }

    @Test
    fun cancelStockNotification_cancelsOnlyTargetMedicationNotification() {
        val med1Id = 10L
        val med2Id = 20L

        notificationHelper.cancelStockNotification(med1Id)

        val expectedMed1NotificationId = notificationHelper.getStockNotificationId(med1Id)
        val expectedMed2NotificationId = notificationHelper.getStockNotificationId(med2Id)

        verify(notificationManager).cancel(eq(expectedMed1NotificationId))
        verify(notificationManager, never()).cancel(eq(expectedMed2NotificationId))
    }
}
