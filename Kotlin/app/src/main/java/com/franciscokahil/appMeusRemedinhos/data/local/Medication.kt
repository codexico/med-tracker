package com.franciscokahil.appMeusRemedinhos.data.local

import kotlinx.serialization.Serializable
import com.franciscokahil.appMeusRemedinhos.R

@Serializable
data class Medication(
    val name: String,
    val dosageValue: String = "",
    val dosageUnit: String = "",
) {
    val displayName: String
        get() {
            if (dosageValue.isEmpty()) return name
            
            // For predefined units (which look like "💊 comprimido"), we only want the emoji in the display name
            // For custom units, we use the whole string.
            val unitDisplay = if (dosageUnit.isNotEmpty() && (dosageUnit[0].isSurrogate() || dosageUnit[0].code > 127)) {
                dosageUnit.split(" ").firstOrNull() ?: dosageUnit
            } else {
                dosageUnit
            }

            return if (unitDisplay.isEmpty()) {
                "$dosageValue $name"
            } else {
                "$dosageValue $unitDisplay $name"
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
    }
}
