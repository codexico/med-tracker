package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import com.franciscokahil.appMeusRemedinhos.R
import java.util.UUID

@Entity(tableName = "medications")
@Serializable
data class Medication(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosageValue: String = "", // Default dosage for this med
    val dosageUnit: String = "",  // Default unit for this med
    val currentStock: Float = 0f,
    val lowStockThreshold: Float = 0f
) {
    val displayName: String
        get() {
            if (dosageValue.isEmpty()) return nameWithEmoji
            
            val unitPart = getEmojiOrUnit()
            return if (unitPart.isEmpty()) {
                "$dosageValue $name"
            } else {
                "$dosageValue $unitPart $name"
            }
        }

    val nameWithEmoji: String
        get() {
            val unitPart = getEmojiOrUnit()
            return if (unitPart.isEmpty()) name else "$unitPart $name"
        }

    private fun getEmojiOrUnit(): String {
        if (dosageUnit.isEmpty()) return ""
        // For predefined units (which look like "💊 comprimido"), we only want the emoji
        // For custom units, we use the whole string.
        return if (dosageUnit[0].isSurrogate() || dosageUnit[0].code > 127) {
            dosageUnit.split(" ").firstOrNull() ?: dosageUnit
        } else {
            dosageUnit
        }
    }
}

data class MedicationUnit(
    val id: String,
    val emoji: String,
    val labelRes: Int
) {
    companion object {
        val PILL = MedicationUnit("pill", "💊", R.string.unit_pill)
        val CAPSULE = MedicationUnit("capsule", "💊", R.string.unit_capsule)
        val MG = MedicationUnit("mg", "⚖️", R.string.unit_mg)
        val ML = MedicationUnit("ml", "🧪", R.string.unit_ml)
        val DROPS = MedicationUnit("drops", "💧", R.string.unit_drops)
        val SPOON = MedicationUnit("spoon", "🥄", R.string.unit_spoon)
        val APPLICATION = MedicationUnit("application", "💉", R.string.unit_application)
        val GRAM = MedicationUnit("gram", "⚖️", R.string.unit_g)
        val UI = MedicationUnit("ui", "💉", R.string.unit_ui)
        val SPRAY = MedicationUnit("spray", "💨", R.string.unit_spray)

        val DEFAULT_UNITS = listOf(PILL, CAPSULE, MG, ML, DROPS, SPOON, APPLICATION, GRAM, UI, SPRAY)
    }
}
