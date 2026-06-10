package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.franciscokahil.appMeusRemedinhos.R

@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var medicationName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_medication),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = medicationName,
                    onValueChange = { 
                        medicationName = it
                        if (it.isNotBlank()) isError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.med_name_placeholder)) },
                    isError = isError,
                    singleLine = true
                )

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
                        if (medicationName.isBlank()) {
                            isError = true
                        } else {
                            onConfirm(medicationName)
                        }
                    }) {
                        Text(stringResource(R.string.add))
                    }
                }
            }
        }
    }
}
