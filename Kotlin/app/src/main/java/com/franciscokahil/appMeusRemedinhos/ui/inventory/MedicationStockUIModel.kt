package com.franciscokahil.appMeusRemedinhos.ui.inventory

import com.franciscokahil.appMeusRemedinhos.data.local.Medication

data class MedicationStockUIModel(
    val medication: Medication,
    val daysRemaining: Int?, // null if consumption is 0
    val dailyDosage: Float,
)
