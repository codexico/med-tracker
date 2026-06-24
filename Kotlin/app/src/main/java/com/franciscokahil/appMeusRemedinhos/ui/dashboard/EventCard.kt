package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.ui.components.M3TimePickerDialog
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: EventEntity,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onSave: (String, String, List<String>) -> Unit,
    onDelete: () -> Unit,
    onToggleTaken: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    highlightColor: Color = Color.Transparent
) {
    // Local state for editing - we keep medications local so we can add them instantly
    var editTitle by remember(event.title, isExpanded) { mutableStateOf(event.title) }
    var editTime by remember(event.time, isExpanded) { mutableStateOf(event.time) }
    var localMedications by remember(event.medications, isExpanded) { mutableStateOf(event.medications) }
    var newMedName by remember(isExpanded) { mutableStateOf("") }
    
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

    val statusText = if (event.isTakenToday) stringResource(R.string.status_taken) else stringResource(R.string.status_pending)
    val cdEventIcon = stringResource(R.string.cd_event_icon)
    val cdMedList = stringResource(R.string.cd_medications_list, event.title)
    val cdMarkTaken = stringResource(R.string.cd_mark_taken, event.title)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isExpanded) Modifier.fillMaxHeight() else Modifier)
            .animateContentSize()
            .clickable(
                enabled = !isExpanded,
                onClickLabel = stringResource(R.string.cd_edit_event, event.title)
            ) { onExpandClick() }
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "${event.title}, ${event.time}, $statusText. ${event.medications.size} medicamentos."
                stateDescription = if (isExpanded) "Expandido" else "Recolhido"
            },
        shape = if (isExpanded) RectangleShape else MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (highlightColor != Color.Transparent) highlightColor 
                             else if (isExpanded) MaterialTheme.colorScheme.surface
                             else if (event.isTakenToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isExpanded) 0.dp else 2.dp)
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
                        Text(text = event.icon, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (event.isTakenToday && !isExpanded) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = event.time,
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
                                event.medications.forEach { med ->
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                text = med,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
                                contentDescription = stringResource(R.string.cd_edit_event, event.title), 
                                modifier = Modifier.size(20.dp), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Checkbox(
                            checked = event.isTakenToday,
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
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { if (it.length <= 25) editTitle = it },
                        label = { Text(stringResource(R.string.time_name_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_event_title_input"),
                        singleLine = true,
                        supportingText = { Text("${editTitle.length}/25", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$editTime",
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
                        verticalArrangement = Arrangement.spacedBy(-8.dp)
                    ) {
                        localMedications.forEachIndexed { index, med ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(med) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_remove_medication, med),
                                        modifier = Modifier
                                            .size(InputChipDefaults.IconSize)
                                            .clickable {
                                                localMedications = localMedications.toMutableList()
                                                    .apply { removeAt(index) }
                                            }
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("medication_chip_$index")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add Med Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newMedName,
                            onValueChange = { 
                                if (it.length <= 30) newMedName = it 
                            },
                            placeholder = { Text(stringResource(R.string.med_name_placeholder)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("medication_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent
                            ),
                            singleLine = true,
                            supportingText = {
                                Row(

                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                )
                                {
                                    Text(text = stringResource(R.string.add_medication_hint))
                                    Text(
                                        text = "${newMedName.length}/30",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        )
                        IconButton(
                            onClick = {
                                if (newMedName.isNotBlank()) {
                                    localMedications = localMedications.toMutableList().apply { add(newMedName.trim()) }
                                    newMedName = ""
                                }
                            },
                            enabled = newMedName.isNotBlank(),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("add_medication_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Footer Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                Icons.Default.DeleteForever, 
                                contentDescription = stringResource(R.string.cd_delete_event, event.title), 
                                tint = MaterialTheme.colorScheme.error
                            )
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
                                    // If there's text in input, add it to medications before saving
                                    val finalMeds = if (newMedName.isNotBlank()) {
                                        localMedications.toMutableList().apply { add(newMedName) }
                                    } else {
                                        localMedications
                                    }
                                    onSave(editTitle, editTime, finalMeds) 
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("save_event_button")
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
fun EventCardPreview() {
    val sampleEvent = EventEntity(
        id = "1",
        title = "Café da manhã",
        time = "08:00",
        medications = listOf("Aspirina", "Vitamina C"),
        isTakenToday = false,
        icon = "🍳"
    )
    
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventCard(
                event = sampleEvent,
                isExpanded = false,
                onExpandClick = {},
                onSave = { _, _, _ -> },
                onDelete = {},
                onToggleTaken = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventCardTakenPreview() {
    val sampleEvent = EventEntity(
        id = "1",
        title = "Ao acordar",
        time = "07:00",
        medications = listOf("Água"),
        isTakenToday = true,
        icon = "🕐"
    )
    
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventCard(
                event = sampleEvent,
                isExpanded = false,
                onExpandClick = {},
                onSave = { _, _, _ -> },
                onDelete = {},
                onToggleTaken = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventCardExpandedPreview() {
    val sampleEvent = EventEntity(
        id = "1",
        title = "Café da manhã",
        time = "08:00",
        medications = listOf("Aspirina", "Cloridato de Vitamina C", "Ômega 3"),
        isTakenToday = false,
        icon = "🍳"
    )
    
    MeusRemedinhosTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            EventCard(
                event = sampleEvent,
                isExpanded = true,
                onExpandClick = {},
                onSave = { _, _, _ -> },
                onDelete = {},
                onToggleTaken = {}
            )
        }
    }
}
