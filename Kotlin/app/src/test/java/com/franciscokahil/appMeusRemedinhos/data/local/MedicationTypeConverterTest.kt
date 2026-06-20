package com.franciscokahil.appMeusRemedinhos.data.local

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MedicationTypeConverter.
 * Tests CSV serialization/deserialization of medication lists.
 */
class MedicationTypeConverterTest {

    private lateinit var converter: MedicationTypeConverter

    @Before
    fun setup() {
        converter = MedicationTypeConverter()
    }

    @Test
    fun `fromList should convert list to CSV string`() {
        // Arrange
        val medications = listOf("Vitamina D", "Ômega-3", "Aspirina")

        // Act
        val csv = converter.fromList(medications)

        // Assert
        assert(csv.isNotEmpty())
        assert(csv.contains("Vitamina D"))
        assert(csv.contains("Ômega-3"))
        assert(csv.contains("Aspirina"))
        assertEquals("Vitamina D,Ômega-3,Aspirina", csv)
    }

    @Test
    fun `fromString should convert CSV back to list`() {
        // Arrange
        val medications = listOf("Vitamina D", "Ômega-3")
        val csv = converter.fromList(medications)

        // Act
        val result = converter.fromString(csv)

        // Assert
        assertEquals(medications, result)
    }

    @Test
    fun `empty list should serialize to empty string`() {
        // Arrange
        val emptyList = emptyList<String>()

        // Act
        val csv = converter.fromList(emptyList)

        // Assert
        assertEquals("", csv)
    }

    @Test
    fun `empty string should deserialize to empty list`() {
        // Arrange
        val emptyString = ""

        // Act
        val result = converter.fromString(emptyString)

        // Assert
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `single medication item should roundtrip correctly`() {
        // Arrange
        val medications = listOf("Remédio Único")

        // Act
        val csv = converter.fromList(medications)
        val result = converter.fromString(csv)

        // Assert
        assertEquals(medications, result)
    }

    @Test
    fun `medicament with spaces should preserve data`() {
        // Arrange
        val medications = listOf(
            "Paracetamol 500mg",
            "Dipirona - Genérico",
            "Vitamina C (ácido)"
        )

        // Act
        val csv = converter.fromList(medications)
        val result = converter.fromString(csv)

        // Assert
        assertEquals(medications, result)
    }

    @Test
    fun `large list should serialize efficiently`() {
        // Arrange
        val medications = (1..100).map { "Remédio $it" }

        // Act
        val csv = converter.fromList(medications)
        val result = converter.fromString(csv)

        // Assert
        assertEquals(medications, result)
        assertEquals(100, result.size)
    }
}

