package com.franciscokahil.appMeusRemedinhos.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme

@Composable
fun DemoContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(8) { i ->
            OutlinedTextField(
                value = "Campo $i",
                onValueChange = {},
                label = { Text("Informação $i") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// OPTION 4.1: FAB to Save, Secondary Actions in Header
@Composable
fun Option4_1ActionsInHeader() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(400.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // HEADER with Title and Close/Delete
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Editar", style = MaterialTheme.typography.titleMedium)
                    }
                    Row {
                        IconButton(onClick = {}) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        IconButton(onClick = {}) { Icon(Icons.Default.Close, contentDescription = null) }
                    }
                }
                
                // Content
                Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
                    DemoContent()
                }
            }
            
            // Floating Action inside the card area
            FloatingActionButton(
                onClick = {},
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Salvar")
            }
        }
    }
}

// OPTION 4.2: FAB to Save, Secondary Actions in Footer (Scrollable)
@Composable
fun Option4_2ActionsInFooter() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(400.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Editar Medicamento", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                
                // Content with actions at the end
                Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
                    Column {
                        DemoContent()
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                            IconButton(onClick = {}) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        }
                    }
                }
            }
            
            // Floating Action inside the card area
            FloatingActionButton(
                onClick = {},
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Salvar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOption4_1() {
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Option4_1ActionsInHeader()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOption4_2() {
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Option4_2ActionsInFooter()
        }
    }
}
