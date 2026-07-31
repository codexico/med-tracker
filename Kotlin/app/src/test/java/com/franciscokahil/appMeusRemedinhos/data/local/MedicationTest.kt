package com.franciscokahil.appMeusRemedinhos.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationTest {

    @Test
    fun `displayName should format correctly for unit with emoji`() {
        // Arrange: "💊 comprimido" is what our UI uses
        val med = Medication(name = "Aspirina", dosageValue = "1", dosageUnit = "💊 comprimido")

        // Act & Assert: Should extract only the emoji
        assertEquals("1 💊 Aspirina", med.displayName)
    }

    @Test
    fun `displayName should format correctly for unit without emoji`() {
        // Arrange: Custom unit from user
        val med = Medication(name = "Curativo", dosageValue = "2", dosageUnit = "adesivo")

        // Act & Assert: Should use full unit string
        assertEquals("2 adesivo Curativo", med.displayName)
    }

    @Test
    fun `displayName should format correctly without unit`() {
        // Arrange: Only name and value
        val med = Medication(name = "Água", dosageValue = "200", dosageUnit = "")

        // Act & Assert
        assertEquals("200 Água", med.displayName)
    }

    @Test
    fun `displayName should return only name if dosage is empty`() {
        // Arrange
        val med = Medication(name = "Vitamina C", dosageValue = "", dosageUnit = "")

        // Act & Assert
        assertEquals("Vitamina C", med.displayName)
    }

    @Test
    fun `displayName should handle multi-character emojis correctly`() {
        // Arrange: Some emojis use surrogate pairs (e.g. 🍽️)
        val med = Medication(name = "Almoço", dosageValue = "1", dosageUnit = "🍽️ refeição")

        // Act & Assert
        assertEquals("1 🍽️ Almoço", med.displayName)
    }
}
