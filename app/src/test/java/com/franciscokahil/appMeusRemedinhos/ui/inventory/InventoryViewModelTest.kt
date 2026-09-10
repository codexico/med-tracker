package com.franciscokahil.appMeusRemedinhos.ui.inventory

import com.franciscokahil.appMeusRemedinhos.background.NotificationHelper
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: MedicationRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(MedicationRepository::class.java)
        notificationHelper = mock(NotificationHelper::class.java)
        viewModel = InventoryViewModel(repository, notificationHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateStock_cancelsNotificationForTargetMedicationOnly() = runTest {
        val targetMedicationId = 1L
        val otherMedicationId = 2L

        viewModel.updateStock(medicationId = targetMedicationId, newQuantity = 10)

        verify(notificationHelper).cancelStockNotification(targetMedicationId)
        verify(notificationHelper, never()).cancelStockNotification(otherMedicationId)
    }

    @Test
    fun addStock_cancelsNotificationForTargetMedicationOnly() = runTest {
        val targetMedicationId = 5L
        val otherMedicationId = 6L

        viewModel.addStock(medicationId = targetMedicationId, amountToAdd = 5)

        verify(notificationHelper).cancelStockNotification(targetMedicationId)
        verify(notificationHelper, never()).cancelStockNotification(otherMedicationId)
    }

    @Test
    fun updateMedication_cancelsNotificationForMedication() = runTest {
        val med = Medication(id = 3L, name = "Paracetamol", quantity = 20, minQuantity = 5)

        viewModel.updateMedication(med)

        verify(notificationHelper).cancelStockNotification(3L)
    }
}
