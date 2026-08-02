package com.franciscokahil.appMeusRemedinhos.ui.inventory

import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventMedicationEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationWithDosage
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val medicationRepository = mockk<MedicationRepository>()
    private val eventRepository = mockk<EventRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `medications flow should calculate daily dosage and days remaining correctly`() = runTest {
        val medication = Medication(id = "1", name = "Test Med", currentStock = 10f)
        val event = EventEntity(id = "1", title = "Event", time = "10:00", isEnabled = true)
        val medicationWithDosage = MedicationWithDosage(
            medication = medication,
            crossRef = EventMedicationEntity("1", "1", dosageValue = "2.0", dosageUnit = "pill")
        )
        val eventWithMedications = EventWithMedications(event, listOf(medicationWithDosage))

        every { medicationRepository.allMedications } returns MutableStateFlow(listOf(medication))
        every { eventRepository.allEvents } returns MutableStateFlow(listOf(eventWithMedications))

        val viewModel = InventoryViewModel(medicationRepository, eventRepository)
        
        val result = viewModel.medications.first { it.isNotEmpty() }
        
        assertEquals(1, result.size)
        assertEquals(2f, result[0].dailyDosage)
        assertEquals(5, result[0].daysRemaining)
    }

    @Test
    fun `daysRemaining should be null if daily dosage is zero`() = runTest {
        val medication = Medication(id = "1", name = "Test Med", currentStock = 10f)

        every { medicationRepository.allMedications } returns MutableStateFlow(listOf(medication))
        every { eventRepository.allEvents } returns MutableStateFlow(emptyList())

        val viewModel = InventoryViewModel(medicationRepository, eventRepository)
        
        val result = viewModel.medications.first { it.isNotEmpty() }
        
        assertEquals(1, result.size)
        assertEquals(0f, result[0].dailyDosage)
        assertNull(result[0].daysRemaining)
    }
}
