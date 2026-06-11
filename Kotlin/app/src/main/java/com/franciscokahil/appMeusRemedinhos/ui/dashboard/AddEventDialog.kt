package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity

@Composable
fun AddEventDialog(
    eventToEdit: EventEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var label by remember { mutableStateOf(eventToEdit?.title ?: "") }
    
    val initialTime = eventToEdit?.time?.split(":")
    val initialHour = initialTime?.get(0)?.toIntOrNull() ?: 12
    val initialMinute = initialTime?.get(1)?.toIntOrNull() ?: 0
    
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }
    var labelError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            hour = h
            // Snapping to half hour intervals
            minute = if (m < 15) 0 else if (m < 45) 30 else 0
            if (m >= 45) {
                hour = (hour + 1) % 24
            }
        },
        hour,
        minute,
        true
    )

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
                    modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = String.format("%02d:%02d", hour, minute),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(
                        onClick = { timePickerDialog.show() },
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
                    if (eventToEdit != null && onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Remover")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (label.isBlank()) {
                            labelError = true
                        } else {
                            onConfirm(label, String.format("%02d:%02d", hour, minute))
                        }
                    }) {
                        Text(if (eventToEdit == null) stringResource(R.string.create) else "Salvar")
                    }
                }
            }
        }
    }
}
