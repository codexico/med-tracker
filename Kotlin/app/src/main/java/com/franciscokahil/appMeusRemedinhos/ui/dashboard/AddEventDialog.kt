package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.ui.components.M3TimePickerDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    eventToEdit: EventEntity? = null,
    initialLabel: String = "",
    initialTimeStr: String = "12:00",
    initialIcon: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var label by remember { mutableStateOf(eventToEdit?.title ?: initialLabel) }
    
    val timeParts = (eventToEdit?.time ?: initialTimeStr).split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 12
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }
    var labelError by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        M3TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { state ->
                hour = state.hour
                minute = state.minute
                showTimePicker = false
            },
            initialHour = hour,
            initialMinute = minute
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (eventToEdit == null) stringResource(R.string.new_time) else stringResource(R.string.edit_time),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.time_name_label),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { 
                        if (it.length <= 25) {
                            label = it
                            if (it.isNotBlank()) labelError = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input"),
                    placeholder = { Text(stringResource(R.string.time_name_placeholder)) },
                    isError = labelError,
                    singleLine = true,
                    maxLines = 1,
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (labelError) {
                                Text(text = stringResource(R.string.name_required_hint), color = MaterialTheme.colorScheme.error)
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            Text(text = "${label.length}/25")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.time_label),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val locale = LocalLocale.current.platformLocale
                    Text(
                        text = String.format(locale, "%02d:%02d", hour, minute),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(
                        onClick = { showTimePicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text(stringResource(R.string.edit_time))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (label.isBlank()) {
                            labelError = true
                        } else {
                            onConfirm(
                                label, 
                                String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                                eventToEdit?.icon ?: initialIcon
                            )
                        }
                    }, modifier = Modifier.testTag("confirm_add_event")) {
                        Text(if (eventToEdit == null) stringResource(R.string.create) else "Salvar")
                    }
                }
            }
        }
    }
}
