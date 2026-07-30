package com.franciscokahil.appMeusRemedinhos.data.local

import kotlinx.serialization.Serializable

@Serializable
data class Medication(
    val name: String,
    val dosageValue: String = "",
    val dosageUnit: String = "",
) {
    val displayName: String
        get() = when {
            dosageValue.isEmpty() -> name
            dosageUnit.isEmpty() -> "$name ($dosageValue)"
            else -> "$name ($dosageValue $dosageUnit)"
        }
}
