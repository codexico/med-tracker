package com.franciscokahil.appMeusRemedinhos.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.franciscokahil.appMeusRemedinhos.background.NotificationHelper
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import com.franciscokahil.appMeusRemedinhos.ui.inventory.InventoryScreen
import com.franciscokahil.appMeusRemedinhos.ui.inventory.InventoryViewModel

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val medicationRepository = MedicationRepository(database.medicationDao())
    val notificationHelper = NotificationHelper(context)
    val inventoryViewModel = InventoryViewModel(medicationRepository, notificationHelper)

    InventoryScreen(viewModel = inventoryViewModel)
}
