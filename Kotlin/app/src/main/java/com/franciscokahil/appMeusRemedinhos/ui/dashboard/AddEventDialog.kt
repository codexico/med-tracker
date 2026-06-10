package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.franciscokahil.appMeusRemedinhos.R
import java.util.Calendar

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(12) }
    var minute by remember { mutableIntStateOf(0) }
    var labelError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            hour = h
            minute = m
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
                    text = stringResource(R.string.new_time),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
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
                        label = it
                        if (it.isNotBlank()) labelError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.time_name_placeholder)) },
                    isError = labelError,
                    supportingText = {
                        if (labelError) {
                            Text(text = stringResource(R.string.name_required_hint), color = MaterialTheme.colorScheme.error)
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
                    Button(onClick = { timePickerDialog.show() }) {
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
                            onConfirm(label, String.format("%02d:%02d", hour, minute))
                        }
                    }) {
                        Text(stringResource(R.string.create))
                    }
                }
            }
        }
    }
}
