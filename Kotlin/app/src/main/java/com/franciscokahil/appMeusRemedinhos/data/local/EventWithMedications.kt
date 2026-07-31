package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class EventWithMedications(
    @Embedded val event: EventEntity,
    @Relation(
        entity = EventMedicationEntity::class,
        parentColumn = "id",
        entityColumn = "eventId"
    )
    val medications: List<MedicationWithDosage>
)

data class MedicationWithDosage(
    @Embedded val crossRef: EventMedicationEntity,
    @Relation(
        parentColumn = "medicationId",
        entityColumn = "id"
    )
    val medication: Medication
) {
    val displayName: String
        get() {
            if (crossRef.dosageValue.isEmpty()) return medication.name
            
            // Extract emoji from predefined units if possible
            val unitDisplay = if (crossRef.dosageUnit.isNotEmpty() && (crossRef.dosageUnit[0].isSurrogate() || crossRef.dosageUnit[0].code > 127)) {
                crossRef.dosageUnit.split(" ").firstOrNull() ?: crossRef.dosageUnit
            } else {
                crossRef.dosageUnit
            }

            return if (unitDisplay.isEmpty()) {
                "${crossRef.dosageValue} ${medication.name}"
            } else {
                "${crossRef.dosageValue} $unitDisplay ${medication.name}"
            }
        }
}
