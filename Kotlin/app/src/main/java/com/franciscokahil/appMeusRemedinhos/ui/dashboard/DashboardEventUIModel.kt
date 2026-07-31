package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications

data class DashboardEventUIModel(
    val eventWithMeds: EventWithMedications,
    val isTakenToday: Boolean
)
