package com.franciscokahil.appMeusRemedinhos.data.local

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MedicationTypeConverter.
 * Tests JSON serialization/deserialization of medication lists.
 */
class MedicationTypeConverterTest {

    private lateinit var converter: MedicationTypeConverter

    @Before
    fun setup() {
        converter = MedicationTypeConverter()
    }

    @Test
    fun `fromList should convert list to JSON string`() {
        // Arrange
        val medications = listOf(
            Medication("Vitamina D", "1", "gota"),
            Medication("Ômega-3", "1000", "mg")
        )

        // Act
        val json = converter.fromList(medications)

        // Assert
        assert(json.isNotEmpty())
        assert(json.contains("Vitamina D"))
        assert(json.contains("gota"))
        assert(json.contains("mg"))
    }

    @Test
    fun `fromString should convert JSON back to list`() {
        // Arrange
        val medications = listOf(Medication("Vitamina D", "1", "gota"))
        val json = converter.fromList(medications)

        // Act
        val result = converter.fromString(json)

        // Assert
        assertEquals(medications, result)
    }

    @Test
    fun `empty list should serialize correctly`() {
        // Arrange
        val emptyList = emptyList<Medication>()

        // Act
        val json = converter.fromList(emptyList)

        // Assert
        assertEquals("[]", json)
    }

    @Test
    fun `empty string should deserialize to empty list`() {
        // Arrange
        val emptyString = ""

        // Act
        val result = converter.fromString(emptyString)

        // Assert
        assertEquals(emptyList<Medication>(), result)
    }

    @Test
    fun `legacy CSV string should be handled correctly`() {
        // Arrange
        val legacyCsv = "Vitamina D, Ômega-3"

        // Act
        val result = converter.fromString(legacyCsv)

        // Assert
        assertEquals(2, result.size)
        assertEquals("Vitamina D", result[0].name)
        assertEquals("Ômega-3", result[1].name)
    }
}
