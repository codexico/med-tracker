package com.franciscokahil.appMeusRemedinhos.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.ui.inventory.MedicationStockUIModel
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val medRepository = remember { MedicationRepositoryImpl(database.medicationDao(), database.doseHistoryDao()) }
    val eventRepository = remember { EventRepositoryImpl(context, database.eventDao()) }
    val factory = remember { InventoryViewModelFactory(medRepository, eventRepository) }
    val viewModel: InventoryViewModel = viewModel(factory = factory)

    val uiModels by viewModel.medications.collectAsState()
    
    var editingMedication by remember { mutableStateOf<Medication?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Medication?>(null) }

    if (editingMedication != null) {
        EditStockDialog(
            medication = editingMedication!!,
            onDismiss = { editingMedication = null },
            onSave = { updatedMed ->
                viewModel.updateMedication(updatedMed)
                editingMedication = null
            },
            onDeleteRequest = { med ->
                showDeleteConfirm = med
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(stringResource(R.string.remove_med_title)) },
            text = { Text(stringResource(R.string.remove_med_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedication(showDeleteConfirm!!)
                        showDeleteConfirm = null
                        editingMedication = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stock_management_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_meds_registered),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiModels, key = { it.medication.id }) { uiModel ->
                    MedicationStockCard(
                        uiModel = uiModel,
                        onClick = { editingMedication = uiModel.medication }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationStockCard(
    uiModel: MedicationStockUIModel,
    onClick: () -> Unit
) {
    val medication = uiModel.medication
    val isLowStock = medication.currentStock <= medication.lowStockThreshold && medication.lowStockThreshold > 0

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.nameWithEmoji,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val stockText = stringResource(R.string.stock_remaining_label, medication.currentStock)
                val daysText = uiModel.daysRemaining?.let { stringResource(R.string.stock_duration_days, it) } ?: ""
                
                Text(
                    text = "$stockText$daysText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }

            if (isLowStock) {
                Text(
                    text = stringResource(R.string.low_stock_label),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun EditStockDialog(
    medication: Medication,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit,
    onDeleteRequest: (Medication) -> Unit
) {
    var stockText by remember { mutableStateOf(medication.currentStock.toString()) }
    var thresholdText by remember { mutableStateOf(medication.lowStockThreshold.toString()) }
    var nameText by remember { mutableStateOf(medication.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_med_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text(stringResource(R.string.time_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text(stringResource(R.string.current_stock_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it },
                    label = { Text(stringResource(R.string.low_stock_threshold_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = medication.copy(
                        name = nameText,
                        currentStock = stockText.toFloatOrNull() ?: 0f,
                        lowStockThreshold = thresholdText.toFloatOrNull() ?: 0f
                    )
                    onSave(updated)
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                IconButton(onClick = { onDeleteRequest(medication) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}
