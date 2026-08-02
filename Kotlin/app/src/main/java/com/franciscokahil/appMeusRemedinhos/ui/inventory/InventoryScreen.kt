package com.franciscokahil.appMeusRemedinhos.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme

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
    
    var expandedMedicationId by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Medication?>(null) }

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
                        expandedMedicationId = null
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
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (expandedMedicationId != null) 0.dp else 12.dp)
            ) {
                items(uiModels, key = { it.medication.id }) { uiModel ->
                    val isExpanded = expandedMedicationId == uiModel.medication.id
                    MedicationStockCard(
                        uiModel = uiModel,
                        isExpanded = isExpanded,
                        onExpandClick = {
                            expandedMedicationId = if (isExpanded) null else uiModel.medication.id
                        },
                        onSave = { updatedMed ->
                            viewModel.updateMedication(updatedMed)
                            expandedMedicationId = null
                        },
                        onDeleteRequest = { med ->
                            showDeleteConfirm = med
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationStockCard(
    uiModel: MedicationStockUIModel,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onSave: (Medication) -> Unit,
    onDeleteRequest: (Medication) -> Unit
) {
    val medication = uiModel.medication
    val isLowStock = medication.currentStock <= medication.lowStockThreshold && medication.lowStockThreshold > 0

    var nameText by remember(medication.name, isExpanded) { mutableStateOf(medication.name) }
    var stockText by remember(medication.currentStock, isExpanded) { mutableStateOf(medication.currentStock.toString()) }
    var thresholdText by remember(medication.lowStockThreshold, isExpanded) { mutableStateOf(medication.lowStockThreshold.toString()) }

    ElevatedCard(
        onClick = onExpandClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = if (isExpanded) 0.dp else 16.dp),
        shape = if (isExpanded) RectangleShape else MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surface
                             else if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isExpanded) 0.dp else 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    
                    val stockTextDisplay = stringResource(R.string.stock_remaining_label, medication.currentStock)
                    val daysText = uiModel.daysRemaining?.let { stringResource(R.string.stock_duration_days, it) } ?: ""
                    
                    Text(
                        text = "$stockTextDisplay$daysText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }

                if (isLowStock && !isExpanded) {
                    Text(
                        text = stringResource(R.string.low_stock_label),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onDeleteRequest(medication) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onExpandClick) {
                                Text(stringResource(R.string.cancel))
                            }
                            Button(
                                onClick = {
                                    val threshold = if (thresholdText.isBlank()) {
                                        uiModel.dailyDosage * 7
                                    } else {
                                        thresholdText.toFloatOrNull() ?: 0f
                                    }
                                    val updated = medication.copy(
                                        name = nameText,
                                        currentStock = stockText.toFloatOrNull() ?: 0f,
                                        lowStockThreshold = threshold
                                    )
                                    onSave(updated)
                                }
                            ) {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationStockCardPreview() {
    val sampleMedication = Medication(
        id = "1",
        name = "Paracetamol 💊",
        currentStock = 10f,
        lowStockThreshold = 5f
    )
    val uiModel = MedicationStockUIModel(
        medication = sampleMedication,
        daysRemaining = 5,
        dailyDosage = 2f
    )
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MedicationStockCard(
                uiModel = uiModel,
                isExpanded = false,
                onExpandClick = {},
                onSave = {},
                onDeleteRequest = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationStockCardExpandedPreview() {
    val sampleMedication = Medication(
        id = "1",
        name = "Paracetamol 💊",
        currentStock = 10f,
        lowStockThreshold = 5f
    )
    val uiModel = MedicationStockUIModel(
        medication = sampleMedication,
        daysRemaining = 5,
        dailyDosage = 2f
    )
    MeusRemedinhosTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            MedicationStockCard(
                uiModel = uiModel,
                isExpanded = true,
                onExpandClick = {},
                onSave = {},
                onDeleteRequest = {}
            )
        }
    }
}
