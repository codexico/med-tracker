package com.franciscokahil.appMeusRemedinhos.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.ui.components.CombinedPreviews
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    highlightedMedId: String? = null,
    onNavigateBack: () -> Unit,
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

    val listState = rememberLazyListState()

    // Handle highlighting from navigation
    LaunchedEffect(highlightedMedId, uiModels) {
        if ((highlightedMedId != null) && uiModels.isNotEmpty()) {
            val index = uiModels.indexOfFirst { it.medication.id == highlightedMedId }
            if (index != -1) {
                expandedMedicationId = highlightedMedId
                delay(100.milliseconds) // Ensure layout is ready
                listState.animateScrollToItem(index)
            }
        }
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
                        expandedMedicationId = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    )
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
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        }
                    ) { med ->
                        showDeleteConfirm = med
                    }
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
    onDeleteRequest: (Medication) -> Unit,
) {
    val medication = uiModel.medication
    val isLowStock = (medication.currentStock <= medication.lowStockThreshold) && (medication.lowStockThreshold > 0)

    val formatFloat = { f: Float -> if ((f % 1f) == 0f) f.toInt().toString() else f.toString() }

    var nameText by remember(medication.name, isExpanded) { mutableStateOf(medication.name) }
    var stockText by remember(medication.currentStock, isExpanded) { mutableStateOf(formatFloat(medication.currentStock)) }
    var thresholdText by remember(medication.lowStockThreshold, isExpanded) { mutableStateOf(formatFloat(medication.lowStockThreshold)) }

    Card(
        onClick = onExpandClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = if (isExpanded) 0.dp else 16.dp)
            .testTag("medication_stock_card_${medication.id}"),
        shape = if (isExpanded) RectangleShape else RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surface
                             else if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 0.dp else 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLowStock && !isExpanded) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) 
                    else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.nameWithEmoji,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val stockValue = formatFloat(medication.currentStock)
                    val stockTextDisplay = if (medication.dosageUnit.isNotEmpty()) {
                        stringResource(R.string.stock_remaining_with_unit_label, stockValue, medication.dosageUnit)
                    } else {
                        stringResource(R.string.stock_remaining_label, stockValue)
                    }
                    
                    val daysText = uiModel.daysRemaining?.let { stringResource(R.string.stock_duration_days, it) } ?: ""
                    
                    Text(
                        text = "$stockTextDisplay$daysText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }

                if (isLowStock && !isExpanded) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("low_stock_emoji")
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
                    
                    Column {
                        Text(
                            text = stringResource(R.string.med_name_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            placeholder = { Text(stringResource(R.string.med_name_placeholder)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.current_stock_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            placeholder = { Text(stringResource(R.string.current_stock_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.low_stock_threshold_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        OutlinedTextField(
                            value = thresholdText,
                            onValueChange = { thresholdText = it },
                            placeholder = { Text(stringResource(R.string.low_stock_threshold_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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

@CombinedPreviews
@Composable
fun MedicationStockCardPreview() {
    val sampleMedication = Medication(
        id = "1",
        name = stringResource(R.string.sample_med_paracetamol),
        currentStock = 10f,
        lowStockThreshold = 5f,
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
                onSave = { }
            ) { }
        }
    }
}

@CombinedPreviews
@Composable
fun MedicationStockCardLowStockPreview() {
    val sampleMedication = Medication(
        id = "2",
        name = stringResource(R.string.sample_med_paracetamol),
        currentStock = 2f,
        lowStockThreshold = 5f,
        dosageUnit = "comprimidos"
    )
    val uiModel = MedicationStockUIModel(
        medication = sampleMedication,
        daysRemaining = 1,
        dailyDosage = 2f
    )
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MedicationStockCard(
                uiModel = uiModel,
                isExpanded = false,
                onExpandClick = {},
                onSave = { }
            ) { }
        }
    }
}

@CombinedPreviews
@Composable
fun MedicationStockCardExpandedPreview() {
    val sampleMedication = Medication(
        id = "1",
        name = stringResource(R.string.sample_med_paracetamol),
        currentStock = 10f,
        lowStockThreshold = 5f,
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
                onSave = { }
            ) { }
        }
    }
}
