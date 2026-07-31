package com.franciscokahil.appMeusRemedinhos.data.repository

import app.cash.turbine.test
import com.franciscokahil.appMeusRemedinhos.data.local.*
import com.franciscokahil.appMeusRemedinhos.ui.inventory.InventoryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InventoryTests {

    private lateinit var medicationRepository: MedicationRepository
    private val medicationDao = mockk<MedicationDao>(relaxed = true)
    private val doseHistoryDao = mockk<DoseHistoryDao>(relaxed = true)
    private val eventRepository = mockk<EventRepository>(relaxed = true)

    @Before
    fun setup() {
        medicationRepository = MedicationRepositoryImpl(medicationDao, doseHistoryDao)
    }

    @Test
    fun `InventoryViewModel should calculate days remaining correctly`() = runTest {
        val med = Medication(id = "med1", name = "Aspirina", currentStock = 10f)
        
        // 2 events using this med: 1 unit and 2 units = 3 units/day
        val event1 = EventWithMedications(
            event = EventEntity(id = "e1", title = "Morning", time = "08:00", isEnabled = true),
            medications = listOf(
                MedicationWithDosage(
                    crossRef = EventMedicationEntity("e1", "med1", "1", ""), 
                    medication = med
                )
            )
        )
        val event2 = EventWithMedications(
            event = EventEntity(id = "e2", title = "Evening", time = "20:00", isEnabled = true),
            medications = listOf(
                MedicationWithDosage(
                    crossRef = EventMedicationEntity("e2", "med1", "2", ""), 
                    medication = med
                )
            )
        )

        // Set up mocks for the repositories
        val mockMedRepo = mockk<MedicationRepository>()
        val mockEventRepo = mockk<EventRepository>()
        
        every { mockMedRepo.allMedications } returns flowOf(listOf(med))
        every { mockMedRepo.allHistory } returns flowOf(emptyList())
        every { mockEventRepo.allEvents } returns flowOf(listOf(event1, event2))

        val viewModel = InventoryViewModel(mockMedRepo, mockEventRepo)
        
        viewModel.medications.test {
            // Wait for non-empty item
            var items = awaitItem()
            if (items.isEmpty()) items = awaitItem()
            
            assertEquals(1, items.size)
            // 10 stock / 3 per day = 3 days remaining
            assertEquals(3, items[0].daysRemaining)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markAsTaken should subtract correct amount from stock`() = runTest {
        val medicationId = "med123"
        val amount = 2.0f
        val timestamp = 1000L
        
        medicationRepository.markAsTaken("event1", medicationId, amount, timestamp)
        
        coVerify { medicationDao.subtractFromStock(medicationId, amount) }
        coVerify { doseHistoryDao.insertDose(match { 
            it.medicationId == medicationId && it.amountTaken == amount && it.status == "TAKEN"
        }) }
    }

    @Test
    fun `markAsSkipped should not change stock but add history entry`() = runTest {
        val medicationId = "med123"
        val timestamp = 2000L
        
        medicationRepository.markAsSkipped("event1", medicationId, timestamp)
        
        coVerify(exactly = 0) { medicationDao.subtractFromStock(any(), any()) }
        coVerify { doseHistoryDao.insertDose(match { 
            it.medicationId == medicationId && it.amountTaken == 0f && it.status == "SKIPPED"
        }) }
    }

    @Test
    fun `insertMedication should use correct mapping to DAO when new`() = runTest {
        val med = Medication(id = "id1", name = "Test Med", currentStock = 50f)
        coEvery { medicationDao.getMedicationByName("Test Med") } returns null
        
        medicationRepository.insertMedication(med)
        
        coVerify { medicationDao.insertMedication(med) }
    }

    @Test
    fun `insertMedication should reuse ID if name already exists`() = runTest {
        val existingMed = Medication(id = "old-id", name = "Aspirina", currentStock = 10f)
        val newMedSameName = Medication(id = "new-id", name = "Aspirina", currentStock = 20f)
        
        coEvery { medicationDao.getMedicationByName("Aspirina") } returns existingMed
        
        val effectiveId = medicationRepository.insertMedication(newMedSameName)
        
        assertEquals("old-id", effectiveId)
        coVerify { medicationDao.updateMedication(match { it.id == "old-id" && it.currentStock == 20f }) }
        coVerify(exactly = 0) { medicationDao.insertMedication(any()) }
    }

    @Test
    fun `updateMedication should call DAO update`() = runTest {
        val med = Medication(id = "id1", name = "Updated Name", lowStockThreshold = 10f)
        
        medicationRepository.updateMedication(med)
        
        coVerify { medicationDao.updateMedication(med) }
    }

    @Test
    fun `deleteMedication should call DAO delete`() = runTest {
        val med = Medication(id = "id1", name = "To Delete")
        
        medicationRepository.deleteMedication(med)
        
        coVerify { medicationDao.deleteMedication(med) }
    }
}
