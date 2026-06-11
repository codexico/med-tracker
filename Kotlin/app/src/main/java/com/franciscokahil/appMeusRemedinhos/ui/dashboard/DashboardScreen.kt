package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.background.AlarmSchedulerImpl
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { EventRepositoryImpl(context, database.eventDao()) }
    val alarmScheduler = remember { AlarmSchedulerImpl(context) }
    val factory = remember { DashboardViewModelFactory(repository, alarmScheduler) }
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    
    val events by viewModel.events.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var eventIdForMedication by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Image(
                        painter = painterResource(id = R.drawable.med_logo_header),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.height(40.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new_time))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (showAddDialog) {
            AddEventDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { label, time ->
                    viewModel.addEvent(label, time)
                    showAddDialog = false
                }
            )
        }

        eventIdForMedication?.let { eventId ->
            AddMedicationDialog(
                onDismiss = { eventIdForMedication = null },
                onConfirm = { medicationName ->
                    viewModel.addMedication(eventId, medicationName)
                    eventIdForMedication = null
                }
            )
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_events),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(
                        time = event.time,
                        title = event.title,
                        medications = event.medications,
                        isTaken = event.isTakenToday,
                        onCheckedChange = { isTaken ->
                            viewModel.toggleEventStatus(event, isTaken)
                        },
                        onAddMedication = {
                            eventIdForMedication = event.id
                        },
                        onRemoveMedication = { index ->
                            viewModel.removeMedication(event.id, index)
                        }
                    )
                }
            }
        }
    }
}
