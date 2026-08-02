package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.data.local.EventMedicationEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationUnit
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationWithDosage
import com.franciscokahil.appMeusRemedinhos.ui.components.M3TimePickerDialog
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: EventWithMedications,
    allMedications: List<Medication>,
    isTakenToday: Boolean,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onSave: (EventEntity, List<Medication>) -> Unit,
    onDelete: () -> Unit,
    onToggleTaken: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    highlightColor: Color = Color.Transparent,
    isNewEvent: Boolean = false,
) {
    // Local state for editing - we keep medications local so we can add them instantly
    var editTitle by remember(event.event.title, isExpanded) { mutableStateOf(event.event.title) }
    var editTime by remember(event.event.time, isExpanded) { mutableStateOf(event.event.time) }
    var editIcon by remember(event.event.icon, isExpanded) { mutableStateOf(event.event.icon) }
    
    // Convert MedicationWithDosage to a flat Medication entity with dosage for local editing
    // This is a bridge between the link table and the main medication table
    val initialMeds = remember(event.medications, isExpanded) {
        event.medications.map { medWithDosage ->
            medWithDosage.medication.copy(
                dosageValue = medWithDosage.crossRef.dosageValue,
                dosageUnit = medWithDosage.crossRef.dosageUnit
            )
        }
    }
    var localMedications by remember(initialMeds, isExpanded) { mutableStateOf(initialMeds) }
    
    // New medication input state
    var newMedName by remember(isExpanded) { mutableStateOf("") }
    var newMedQuantity by remember(isExpanded) { mutableStateOf("") }
    var newMedUnit by remember(isExpanded) { mutableStateOf("") }
    var newMedStock by remember(isExpanded) { mutableStateOf("") }
    var newMedThreshold by remember(isExpanded) { mutableStateOf("") }
    var customUnit by remember(isExpanded) { mutableStateOf("") }
    var unitExpanded by remember { mutableStateOf(value = false) }
    var medNameError by remember { mutableStateOf(false) }
    var medNameExpanded by remember { mutableStateOf(false) }
    var selectedMedicationId by remember(isExpanded) { mutableStateOf<String?>(null) }
    
    // Editing medication state
    var editingMedicationIndex by remember(isExpanded) { mutableStateOf<Int?>(null) }
    
    var titleError by remember { mutableStateOf(false) }
    
    val otherUnitLabel = stringResource(R.string.unit_other)
    val units = listOf(
        "${MedicationUnit.PILL.emoji} ${stringResource(MedicationUnit.PILL.labelRes)}",
        "${MedicationUnit.CAPSULE.emoji} ${stringResource(MedicationUnit.CAPSULE.labelRes)}",
        "${MedicationUnit.MG.emoji} ${stringResource(MedicationUnit.MG.labelRes)}",
        "${MedicationUnit.ML.emoji} ${stringResource(MedicationUnit.ML.labelRes)}",
        "${MedicationUnit.DROPS.emoji} ${stringResource(MedicationUnit.DROPS.labelRes)}",
        "${MedicationUnit.SPOON.emoji} ${stringResource(MedicationUnit.SPOON.labelRes)}",
        "${MedicationUnit.APPLICATION.emoji} ${stringResource(MedicationUnit.APPLICATION.labelRes)}",
        otherUnitLabel,
    )

    val timeParts = editTime.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 12
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        M3TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { state ->
                editTime = String.format(Locale.getDefault(), "%02d:%02d", state.hour, state.minute)
                showTimePicker = false
            },
            initialHour = initialHour,
            initialMinute = initialMinute
        )
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    var showMedDeleteConfirm by remember { mutableStateOf(false) }

    if (showMedDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showMedDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMedDeleteConfirm = false
                        editingMedicationIndex?.let { index ->
                            localMedications = localMedications.toMutableList().apply { removeAt(index) }
                            editingMedicationIndex = null
                            newMedName = ""
                            newMedQuantity = ""
                            newMedUnit = ""
                            customUnit = ""
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMedDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val statusText = if (isTakenToday) stringResource(R.string.status_taken) else stringResource(R.string.status_pending)
    val cdEventIcon = stringResource(R.string.cd_event_icon)
    val cdMedList = stringResource(R.string.cd_medications_list, event.event.title)
    val cdMarkTaken = stringResource(R.string.cd_mark_taken, event.event.title)

    val medicationsCountText = stringResource(R.string.medications_count, event.medications.size)
    val expandedText = stringResource(R.string.status_expanded)
    val collapsedText = stringResource(R.string.status_collapsed)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isExpanded) Modifier.fillMaxHeight() else Modifier)
            .animateContentSize()
            .then(
                if (!isExpanded) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium
                ) else Modifier
            )
            .clickable(
                enabled = !isExpanded,
                onClickLabel = stringResource(R.string.cd_edit_event, event.event.title)
            ) { onExpandClick() }
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "${event.event.title}, ${event.event.time}, $statusText. $medicationsCountText"
                stateDescription = if (isExpanded) expandedText else collapsedText
            },
        shape = if (isExpanded) RectangleShape else MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (highlightColor != Color.Transparent) highlightColor 
                             else if (isExpanded) MaterialTheme.colorScheme.surface
                             else if (isTakenToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isExpanded) 0.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isExpanded) Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()) else Modifier
                )
        ) {
            // HEADER (Always visible)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color.Transparent)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon/Emoji Box
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                            .semantics { contentDescription = cdEventIcon },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isExpanded) editIcon else event.event.icon, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isExpanded) editTitle.ifBlank { stringResource(R.string.new_time) } else event.event.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (!isExpanded && isTakenToday) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (isExpanded) editTime else event.event.time,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                        
                        // COMPACT MEDICATION LIST (Chips) - Always visible in compact state
                        if (!isExpanded && event.medications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { contentDescription = cdMedList },
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                event.medications.forEach { medWithDosage ->
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                text = medWithDosage.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        border = null,
                                        shape = MaterialTheme.shapes.extraSmall,
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (!isExpanded) {
                        IconButton(
                            onClick = onExpandClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_event_button")
                        ) {
                            Icon(
                                Icons.Default.Edit, 
                                contentDescription = stringResource(R.string.cd_edit_event, event.event.title), 
                                modifier = Modifier.size(20.dp), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Checkbox(
                            checked = isTakenToday,
                            onCheckedChange = onToggleTaken,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("event_checkbox")
                                .semantics {
                                    contentDescription = cdMarkTaken
                                }
                        )
                    }
                }
            }

            // EXPANDED CONTENT
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit Title
                    val isOtherEvent = event.event.type == EventType.OTHER
                    if (isOtherEvent) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = {
                                if (it.length <= 25) {
                                    editTitle = it
                                    if (it.isNotBlank()) titleError = false
                                }
                            },
                            label = { Text(stringResource(R.string.time_name_label)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_event_title_input"),
                            singleLine = true,
                            isError = titleError,
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    if (titleError) {
                                        Text(
                                            text = stringResource(R.string.name_required_hint),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                    Text("${editTitle.length}/25")
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Edit Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = editTime,
                            style = MaterialTheme.typography.displaySmall
                        )
                        Button(
                            onClick = { showTimePicker = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(stringResource(R.string.edit_time))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.medications_label), 
                        style = MaterialTheme.typography.labelLarge, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    // Med List (Full version with Chips)
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("medications_flow_row"),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy((-8).dp)
                    ) {
                        localMedications.forEachIndexed { index, med ->
                            val isEditingThis = editingMedicationIndex == index
                            InputChip(
                                selected = isEditingThis,
                                onClick = {
                                    editingMedicationIndex = index
                                    newMedName = med.name
                                    newMedQuantity = med.dosageValue
                                    newMedStock = if (med.currentStock > 0) med.currentStock.toString() else ""
                                    newMedThreshold = if (med.lowStockThreshold > 0) med.lowStockThreshold.toString() else ""
                                    
                                    // Robust unit detection
                                    val matchedUnit = units.find { it == med.dosageUnit }
                                    if (matchedUnit != null) {
                                        newMedUnit = matchedUnit
                                        customUnit = ""
                                    } else if (med.dosageUnit.isNotEmpty()) {
                                        newMedUnit = otherUnitLabel
                                        customUnit = med.dosageUnit
                                    } else {
                                        newMedUnit = ""
                                        customUnit = ""
                                    }
                                    medNameError = false
                                },
                                label = { Text(text = med.displayName) },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("medication_chip_$index")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // REFINED ADD/EDIT MED INPUT
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (editingMedicationIndex != null) "Editar item" else "Adicionar item",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Line 1: Name with Autocomplete
                            ExposedDropdownMenuBox(
                                expanded = medNameExpanded && newMedName.isNotBlank(),
                                onExpandedChange = { medNameExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextField(
                                    value = newMedName,
                                    onValueChange = { 
                                        if (it.length <= 30) {
                                            newMedName = it
                                            medNameExpanded = true
                                            if (it.isNotBlank()) medNameError = false
                                            
                                            // Check if it matches an existing med EXACTLY to reuse ID
                                            val matched = allMedications.find { m -> m.name.equals(it.trim(), ignoreCase = true) }
                                            if (matched != null) {
                                                selectedMedicationId = matched.id
                                                // We don't auto-fill everything here to avoid annoying the user 
                                                // while typing, but the ID is now linked.
                                            } else {
                                                selectedMedicationId = null
                                            }
                                        }
                                    },
                                    placeholder = { Text(stringResource(R.string.med_name_placeholder)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                                        .testTag("medication_input"),
                                    singleLine = true,
                                    isError = medNameError,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = if (medNameError) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                val filteredOptions = allMedications.filter { 
                                    it.name.contains(newMedName, ignoreCase = true) 
                                }.take(5)

                                if (filteredOptions.isNotEmpty()) {
                                    ExposedDropdownMenu(
                                        expanded = medNameExpanded && newMedName.isNotBlank(),
                                        onDismissRequest = { medNameExpanded = false }
                                    ) {
                                        filteredOptions.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(selectionOption.name) },
                                                onClick = {
                                                    selectedMedicationId = selectionOption.id
                                                    newMedName = selectionOption.name
                                                    newMedQuantity = selectionOption.dosageValue
                                                    newMedUnit = if (selectionOption.dosageUnit in units) selectionOption.dosageUnit else if (selectionOption.dosageUnit.isNotEmpty()) otherUnitLabel else ""
                                                    customUnit = if (selectionOption.dosageUnit in units) "" else selectionOption.dosageUnit
                                                    newMedStock = if (selectionOption.currentStock > 0) selectionOption.currentStock.toString() else ""
                                                    newMedThreshold = if (selectionOption.lowStockThreshold > 0) selectionOption.lowStockThreshold.toString() else ""
                                                    medNameExpanded = false
                                                    medNameError = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = newMedQuantity,
                                    onValueChange = { newMedQuantity = it },
                                    placeholder = { Text(stringResource(R.string.med_qty_placeholder)) },
                                    modifier = Modifier.weight(0.8f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                                    ),
                                )
                                
                                ExposedDropdownMenuBox(
                                    expanded = unitExpanded,
                                    onExpandedChange = { unitExpanded = !unitExpanded },
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    TextField(
                                        value = newMedUnit,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = { Text(stringResource(R.string.unit_label)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                    ExposedDropdownMenu(
                                        expanded = unitExpanded,
                                        onDismissRequest = { unitExpanded = false }
                                    ) {
                                        units.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(selectionOption) },
                                                onClick = {
                                                    newMedUnit = selectionOption
                                                    unitExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            if (newMedUnit == otherUnitLabel) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = customUnit,
                                    onValueChange = { customUnit = it },
                                    placeholder = { Text(stringResource(R.string.unit_other)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // STOCK FIELDS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = newMedStock,
                                    onValueChange = { newMedStock = it },
                                    label = { Text("Estoque Atual") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                TextField(
                                    value = newMedThreshold,
                                    onValueChange = { newMedThreshold = it },
                                    label = { Text("Aviso de estoque") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                            }
                            
                            if (medNameError) {
                                Text(
                                    text = stringResource(R.string.med_name_error),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (editingMedicationIndex == null) {
                                // ADD MODE
                                Button(
                                    onClick = { 
                                        if (newMedName.isBlank()) {
                                            medNameError = true
                                        } else {
                                            val finalUnit = if (newMedUnit == otherUnitLabel) customUnit.trim() else newMedUnit
                                        val stock = newMedStock.toFloatOrNull() ?: 0f
                                        val threshold = newMedThreshold.toFloatOrNull() ?: 0f
                                        
                                        localMedications = localMedications.toMutableList().apply { 
                                            val newMed = if (selectedMedicationId != null) {
                                                Medication(
                                                    id = selectedMedicationId!!,
                                                    name = newMedName.trim(),
                                                    dosageValue = newMedQuantity.trim(),
                                                    dosageUnit = finalUnit,
                                                    currentStock = stock,
                                                    lowStockThreshold = threshold
                                                )
                                            } else {
                                                Medication(
                                                    name = newMedName.trim(), 
                                                    dosageValue = newMedQuantity.trim(), 
                                                    dosageUnit = finalUnit,
                                                    currentStock = stock,
                                                    lowStockThreshold = threshold
                                                )
                                            }
                                            add(newMed) 
                                        }
                                        newMedName = ""
                                        newMedQuantity = ""
                                        newMedUnit = ""
                                        newMedStock = ""
                                        newMedThreshold = ""
                                        customUnit = ""
                                        selectedMedicationId = null
                                        medNameError = false
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("add_medication_button"),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.add_medication_hint))
                                }
                            } else {
                                // EDIT MODE
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { showMedDeleteConfirm = true },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.remove), fontSize = 13.sp)
                                    }

                                    TextButton(
                                        onClick = {
                                            editingMedicationIndex = null
                                            newMedName = ""
                                            newMedQuantity = ""
                                            newMedUnit = ""
                                            customUnit = ""
                                            medNameError = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(stringResource(R.string.cancel), fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (newMedName.isBlank()) {
                                                medNameError = true
                                            } else {
                                                val finalUnit = if (newMedUnit == otherUnitLabel) customUnit.trim() else newMedUnit
                                                val stock = newMedStock.toFloatOrNull() ?: 0f
                                                val threshold = newMedThreshold.toFloatOrNull() ?: 0f
                                                
                                                val updatedMed = localMedications[editingMedicationIndex!!].copy(
                                                    id = selectedMedicationId ?: localMedications[editingMedicationIndex!!].id,
                                                    name = newMedName.trim(),
                                                    dosageValue = newMedQuantity.trim(),
                                                    dosageUnit = finalUnit,
                                                    currentStock = stock,
                                                    lowStockThreshold = threshold
                                                )
                                                localMedications = localMedications.toMutableList().apply {
                                                    set(editingMedicationIndex!!, updatedMed)
                                                }
                                                editingMedicationIndex = null
                                                newMedName = ""
                                                newMedQuantity = ""
                                                newMedUnit = ""
                                                newMedStock = ""
                                                newMedThreshold = ""
                                                customUnit = ""
                                                selectedMedicationId = null
                                                medNameError = false
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = MaterialTheme.shapes.small,
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(stringResource(R.string.save), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Footer Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isNewEvent) {
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = stringResource(R.string.cd_delete_event, event.event.title),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp)) // Maintain alignment
                        }
                        
                        Row {
                            TextButton(
                                onClick = onExpandClick,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { 
                                    if (editTitle.isBlank()) {
                                        titleError = true
                                    } else {
                                        val finalMeds = if (newMedName.isNotBlank()) {
                                            val finalUnit = if (newMedUnit == otherUnitLabel) customUnit.trim() else newMedUnit
                                            val stock = newMedStock.toFloatOrNull() ?: 0f
                                            val threshold = newMedThreshold.toFloatOrNull() ?: 0f
                                            
                                            val currentMed = Medication(
                                                name = newMedName.trim(), 
                                                dosageValue = newMedQuantity.trim(), 
                                                dosageUnit = finalUnit,
                                                currentStock = stock,
                                                lowStockThreshold = threshold
                                            )
                                            
                                            localMedications.toMutableList().apply { 
                                                if (editingMedicationIndex != null) {
                                                    set(editingMedicationIndex!!, currentMed)
                                                } else {
                                                    add(currentMed)
                                                }
                                            }
                                        } else {
                                            localMedications
                                        }
                                        
                                        val finalEvent = event.event.copy(
                                            title = editTitle,
                                            time = editTime,
                                            icon = editIcon
                                        )
                                        onSave(finalEvent, finalMeds)
                                    }
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("save_event_button")
                            ) {
                                Text(if (isNewEvent) stringResource(R.string.create) else stringResource(R.string.save))
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
fun EventCardPreview() {
    val sampleEvent = EventWithMedications(
        event = EventEntity(id = "1", title = "Café da manhã", time = "08:00", icon = "🍳"),
        medications = listOf(
            MedicationWithDosage(
                crossRef = EventMedicationEntity("1", "1", "1", "💊 comprimido"),
                medication = Medication(id = "1", name = "Aspirina")
            ),
            MedicationWithDosage(
                crossRef = EventMedicationEntity("1", "2", "1", "💊 comprimido"),
                medication = Medication(id = "2", name = "Vitamina C")
            )
        )
    )
    
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventCard(
                event = sampleEvent,
                allMedications = emptyList(),
                isTakenToday = false,
                isExpanded = false,
                onExpandClick = {},
                onSave = { _, _ -> },
                onDelete = {},
                onToggleTaken = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventCardTakenPreview() {
    val sampleEvent = EventWithMedications(
        event = EventEntity(id = "1", title = "Ao acordar", time = "07:00", icon = "🕐"),
        medications = listOf(
            MedicationWithDosage(
                crossRef = EventMedicationEntity("1", "3", "200", "ml"),
                medication = Medication(id = "3", name = "Água")
            )
        )
    )
    
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventCard(
                event = sampleEvent,
                allMedications = emptyList(),
                isTakenToday = true,
                isExpanded = false,
                onExpandClick = {},
                onSave = { _, _ -> },
                onDelete = {},
                onToggleTaken = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventCardExpandedPreview() {
    val sampleEvent = EventWithMedications(
        event = EventEntity(id = "1", title = "Café da manhã", time = "08:00", icon = "🍳"),
        medications = listOf(
            MedicationWithDosage(
                crossRef = EventMedicationEntity("1", "1", "100", "mg"),
                medication = Medication(id = "1", name = "Aspirina", currentStock = 24f, lowStockThreshold = 5f)
            ),
            MedicationWithDosage(
                crossRef = EventMedicationEntity("1", "2", "1", "💊 comprimido"),
                medication = Medication(id = "2", name = "Cloridato de Vitamina C")
            )
        )
    )
    
    MeusRemedinhosTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            EventCard(
                event = sampleEvent,
                allMedications = listOf(Medication(id = "1", name = "Aspirina"), Medication(id = "2", name = "Vitamina C")),
                isTakenToday = false,
                isExpanded = true,
                onExpandClick = {},
                onSave = { _, _ -> },
                onDelete = {},
                onToggleTaken = {}
            )
        }
    }
}
