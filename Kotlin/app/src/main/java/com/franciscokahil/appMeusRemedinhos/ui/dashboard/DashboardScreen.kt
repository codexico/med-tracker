package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.background.AlarmSchedulerImpl
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(highlightedId: String? = null) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { EventRepositoryImpl(context, database.eventDao()) }
    val alarmScheduler = remember { AlarmSchedulerImpl(context) }
    val factory = remember { DashboardViewModelFactory(repository, alarmScheduler) }
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    
    val events by viewModel.events.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<EventEntity?>(null) }
    var eventIdForMedication by remember { mutableStateOf<String?>(null) }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var activeHighlightId by remember { mutableStateOf<String?>(null) }

    // Scroll to highlighted item
    LaunchedEffect(highlightedId, events) {
        if (highlightedId != null && events.isNotEmpty()) {
            val index = events.indexOfFirst { it.id == highlightedId }
            if (index != -1) {
                delay(500) // Give UI time to stabilize
                listState.animateScrollToItem(index)
                activeHighlightId = highlightedId
                delay(2000)
                activeHighlightId = null
            }
        }
    }

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
        if (showAddDialog || eventToEdit != null) {
            AddEventDialog(
                eventToEdit = eventToEdit,
                onDismiss = { 
                    showAddDialog = false
                    eventToEdit = null
                },
                onConfirm = { label, time ->
                    if (eventToEdit == null) {
                        viewModel.addEvent(label, time)
                    } else {
                        viewModel.updateEvent(eventToEdit!!, label, time)
                    }
                    showAddDialog = false
                    eventToEdit = null
                },
                onDelete = if (eventToEdit != null) {
                    {
                        viewModel.deleteEvent(eventToEdit!!)
                        eventToEdit = null
                    }
                } else null
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
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    val isHighlighted = activeHighlightId == event.id
                    val elevation by animateColorAsState(
                        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        animationSpec = tween(500)
                    )

                    Box(modifier = Modifier.background(elevation)) {
                        EventCard(
                            time = event.time,
                            title = event.title,
                            icon = event.icon,
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
                            },
                            onEditClick = {
                                eventToEdit = event
                            }
                        )
                    }
                }
            }
        }
    }
}
