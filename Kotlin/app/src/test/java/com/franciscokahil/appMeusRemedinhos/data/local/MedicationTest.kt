package com.franciscokahil.appMeusRemedinhos.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationTest {

    @Test
    fun `displayName should format correctly for unit with emoji`() {
        // Arrange: "💊 comprimido" is what our UI uses
        val med = Medication("Aspirina", "1", "💊 comprimido")

        // Act & Assert: Should extract only the emoji
        assertEquals("1 💊 Aspirina", med.displayName)
    }

    @Test
    fun `displayName should format correctly for unit without emoji`() {
        // Arrange: Custom unit from user
        val med = Medication("Curativo", "2", "adesivo")

        // Act & Assert: Should use full unit string
        assertEquals("2 adesivo Curativo", med.displayName)
    }

    @Test
    fun `displayName should format correctly without unit`() {
        // Arrange: Only name and value
        val med = Medication("Água", "200", "")

        // Act & Assert
        assertEquals("200 Água", med.displayName)
    }

    @Test
    fun `displayName should return only name if dosage is empty`() {
        // Arrange
        val med = Medication("Vitamina C", "", "")

        // Act & Assert
        assertEquals("Vitamina C", med.displayName)
    }

    @Test
    fun `displayName should handle multi-character emojis correctly`() {
        // Arrange: Some emojis use surrogate pairs (e.g. 🍽️)
        val med = Medication("Almoço", "1", "🍽️ refeição")

        // Act & Assert
        assertEquals("1 🍽️ Almoço", med.displayName)
    }
}
