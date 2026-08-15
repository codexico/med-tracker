package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.background.AlarmSchedulerImpl
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.data.local.EventMedicationEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationWithDosage
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.ui.components.CombinedPreviews
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToInventory: (String?) -> Unit,
    highlightedId: String? = null,
    onHighlightedConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val eventRepository = remember { EventRepositoryImpl(context, database.eventDao()) }
    val medicationRepository = remember { MedicationRepositoryImpl(database.medicationDao(), database.doseHistoryDao()) }
    val alarmScheduler = remember { AlarmSchedulerImpl(context) }
    val factory = remember { DashboardViewModelFactory(eventRepository, medicationRepository, alarmScheduler) }
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    
    val events by viewModel.events.collectAsState()
    val pendingEvents by viewModel.pendingEvents.collectAsState()
    val allMedications by viewModel.allMedications.collectAsState()
    val shouldShowOnboarding by viewModel.shouldShowOnboarding.collectAsState()
    var expandedEventId by remember { mutableStateOf<String?>(null) }
    
    // FAB Menu State
    var isFabExpanded by remember { mutableStateOf(false) }
    var pendingNewEvent by remember { mutableStateOf<EventWithMedications?>(null) }
    var pendingNavigationMedId by remember { mutableStateOf<String?>(null) }
    
    // Tooltip State (shared for onboarding)
    val tooltipState = remember { TooltipState() }
    
    // Pulsating animation for the tooltip (pulsing towards the FAB)
    val infiniteTransition = rememberInfiniteTransition(label = "tooltip")
    val tooltipOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Handle tooltip visibility: only show if no events exist
    LaunchedEffect(shouldShowOnboarding, isFabExpanded) {
        if (shouldShowOnboarding) {
            delay(500.milliseconds) // Small delay to ensure UI is ready
            tooltipState.show()
        } else {
            tooltipState.dismiss()
        }
    }
    
    // Permission State
    var showPermissionExplanation by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Proceed with adding the event regardless of permission result
        pendingNewEvent?.let { params ->
            val medications = params.medications.map { it.medication.copy(
                dosageValue = it.crossRef.dosageValue,
                dosageUnit = it.crossRef.dosageUnit
            ) }
            viewModel.addEvent(params.event.title, params.event.time, params.event.icon, medications, params.event.type)
            
            pendingNavigationMedId?.let { navId ->
                onNavigateToInventory(navId)
            }
        }
        pendingNewEvent = null
        pendingNavigationMedId = null
        expandedEventId = null
    }

    val listState = rememberLazyListState()
    var activeHighlightId by remember { mutableStateOf<String?>(null) }

    // Scroll to highlighted item from widget deep-link
    LaunchedEffect(highlightedId, events) {
        if (highlightedId != null && events.isNotEmpty()) {
            // Auto-collapse any expanded card when navigating via deep-link
            expandedEventId = null

            val index = events.indexOfFirst { it.eventWithMeds.event.id == highlightedId }
            if (index != -1) {
                // Wait for LazyColumn to be laid out and ready for scrolling
                delay(200.milliseconds)

                // Scroll to the item and position it near the top
                listState.animateScrollToItem(index = index, scrollOffset = 0)
                
                // Trigger visual highlight feedback
                activeHighlightId = highlightedId

                // Keep highlight visible for 2 seconds
                delay(2000.milliseconds)

                // Clear highlight and consume the highlighted ID
                activeHighlightId = null
                onHighlightedConsumed()
            }
        }
    }

    val createNewEvent = { label: String, time: String, icon: String?, meds: List<Medication>, type: EventType, navMedId: String? ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                viewModel.addEvent(label, time, icon, meds, type)
                expandedEventId = null
                pendingNewEvent = null
                if (navMedId != null) onNavigateToInventory(navMedId)
            } else {
                pendingNewEvent = EventWithMedications(
                    event = EventEntity(
                        id = "NEW_EVENT",
                        title = label,
                        time = time,
                        icon = icon ?: "access_time",
                        type = type
                    ),
                    medications = meds.map { med ->
                        MedicationWithDosage(
                            crossRef = EventMedicationEntity("NEW_EVENT", med.id, med.dosageValue, med.dosageUnit),
                            medication = med
                        )
                    }
                )
                pendingNavigationMedId = navMedId
                showPermissionExplanation = true
            }
        } else {
            viewModel.addEvent(label, time, icon, meds, type)
            expandedEventId = null
            pendingNewEvent = null
            if (navMedId != null) onNavigateToInventory(navMedId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Icon(
                        painter = painterResource(id = R.drawable.med_logo_header_dynamic),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.fillMaxWidth().height(40.dp).padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { onNavigateToInventory(null) }) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Stock",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = expandedEventId == null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FabMenu(
                    isExpanded = isFabExpanded,
                    onToggle = { isFabExpanded = !isFabExpanded },
                    onOptionSelected = { preset ->
                        isFabExpanded = false
                        pendingNewEvent = EventWithMedications(
                            event = EventEntity(
                                id = "NEW_EVENT",
                                title = if (preset?.type == EventType.OTHER) "" else (preset?.label ?: ""),
                                time = preset?.time ?: "12:00",
                                icon = preset?.icon ?: "access_time",
                                type = preset?.type ?: EventType.OTHER
                            ),
                            medications = emptyList()
                        )
                        expandedEventId = "NEW_EVENT"
                    },
                    tooltipState = tooltipState,
                    tooltipOffsetX = tooltipOffsetX
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // LOW STOCK BANNER
            val lowStockMeds = allMedications.filter { it.currentStock <= it.lowStockThreshold && it.lowStockThreshold > 0 }
            if (lowStockMeds.isNotEmpty() && expandedEventId == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.stock_banner_title),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            val medNames = lowStockMeds.take(2).joinToString(", ") { it.name }
                            val suffix = if (lowStockMeds.size > 2) stringResource(R.string.stock_banner_more) else ""
                            Text(
                                text = "$medNames$suffix",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        TextButton(onClick = { onNavigateToInventory(null) }) {
                            Text(stringResource(R.string.stock_banner_action), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (showPermissionExplanation) {
                AlertDialog(
                    onDismissRequest = { 
                        showPermissionExplanation = false
                        // Proceed anyway
                        pendingNewEvent?.let { params ->
                            val medications = params.medications.map { it.medication.copy(
                                dosageValue = it.crossRef.dosageValue,
                                dosageUnit = it.crossRef.dosageUnit
                            ) }
                            viewModel.addEvent(params.event.title, params.event.time, params.event.icon, medications, params.event.type)
                            pendingNavigationMedId?.let { onNavigateToInventory(it) }
                        }
                        pendingNewEvent = null
                        pendingNavigationMedId = null
                        expandedEventId = null
                    },
                    title = { Text(stringResource(R.string.permission_dialog_title)) },
                    text = { Text(stringResource(R.string.permission_dialog_desc)) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    confirmButton = {
                        TextButton(onClick = {
                            showPermissionExplanation = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }, modifier = Modifier.testTag("permission_confirm_button")) {
                            Text(stringResource(R.string.permission_dialog_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showPermissionExplanation = false
                            pendingNewEvent?.let { params ->
                                val medications = params.medications.map { it.medication.copy(
                                    dosageValue = it.crossRef.dosageValue,
                                    dosageUnit = it.crossRef.dosageUnit
                                ) }
                                viewModel.addEvent(params.event.title, params.event.time, params.event.icon, medications, params.event.type)
                                pendingNavigationMedId?.let { onNavigateToInventory(it) }
                            }
                            pendingNewEvent = null
                            pendingNavigationMedId = null
                            expandedEventId = null
                        }) {
                            Text(stringResource(R.string.permission_dialog_cancel))
                        }
                    }
                )
            }

            if (events.isEmpty() && expandedEventId != "NEW_EVENT") {
                OnboardingEmptyState(
                    paddingValues = PaddingValues(0.dp),
                    onAddClick = { isFabExpanded = true },
                    modifier = Modifier.testTag("empty_state")
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("event_list"),
                    verticalArrangement = if (expandedEventId == null) Arrangement.spacedBy(16.dp) else Arrangement.Top,
                    contentPadding = if (expandedEventId == null) PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 8.dp) else PaddingValues(0.dp)
                ) {
                    // PENDING EVENTS FROM YESTERDAY
                    if (pendingEvents.isNotEmpty() && expandedEventId == null) {
                        item {
                            Text(
                                text = stringResource(R.string.pending_yesterday_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(pendingEvents) { event ->
                            PendingEventCard(
                                event = event,
                                onTakenLate = { viewModel.markAsTakenRetrospectively(event) },
                                onSkip = { viewModel.markAsSkippedRetrospectively(event) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (expandedEventId == "NEW_EVENT" && pendingNewEvent != null) {
                        item(key = "NEW_EVENT") {
                            EventCard(
                                event = pendingNewEvent!!,
                                allMedications = allMedications,
                                isTakenToday = false,
                                isExpanded = true,
                                onExpandClick = {
                                    expandedEventId = null
                                    pendingNewEvent = null
                                },
                                onSave = { updatedEvent, meds ->
                                    createNewEvent(updatedEvent.title, updatedEvent.time, updatedEvent.icon, meds, updatedEvent.type, null)
                                },
                                onManageStock = { updatedEvent, meds, targetMedId ->
                                    // Save state first (it will navigate later if permission is needed)
                                    createNewEvent(updatedEvent.title, updatedEvent.time, updatedEvent.icon, meds, updatedEvent.type, targetMedId)
                                },
                                onDelete = { },
                                onToggleTaken = { },
                                modifier = Modifier.fillParentMaxSize(),
                                isNewEvent = true
                            )
                        }
                    }

                    items(events, key = { it.eventWithMeds.event.id }) { uiModel ->
                        val eventWithMeds = uiModel.eventWithMeds
                        val isHighlighted = activeHighlightId == eventWithMeds.event.id
                        val isExpanded = expandedEventId == eventWithMeds.event.id
                        
                        val highlightColor by animateColorAsState(
                            targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                            animationSpec = tween(500), label = "highlight"
                        )

                        if (expandedEventId == null || isExpanded) {
                            EventCard(
                                event = eventWithMeds,
                                allMedications = allMedications,
                                isTakenToday = uiModel.isTakenToday,
                                isExpanded = isExpanded,
                                onExpandClick = {
                                    expandedEventId = if (isExpanded) null else eventWithMeds.event.id
                                },
                                onSave = { updatedEvent, meds ->
                                    viewModel.updateEvent(updatedEvent, updatedEvent.title, updatedEvent.time, meds, updatedEvent.type)
                                    expandedEventId = null
                                },
                                onManageStock = { updatedEvent, meds, targetMedId ->
                                    // Save state first
                                    viewModel.updateEvent(updatedEvent, updatedEvent.title, updatedEvent.time, meds, updatedEvent.type)
                                    expandedEventId = null
                                    // Navigate to inventory with the med highlighted
                                    onNavigateToInventory(targetMedId)
                                },
                                onDelete = {
                                    viewModel.deleteEvent(eventWithMeds.event)
                                    expandedEventId = null
                                },
                                onToggleTaken = { isTaken ->
                                    viewModel.toggleEventStatus(eventWithMeds, isTaken)
                                },
                                highlightColor = highlightColor,
                                modifier = if (isExpanded) Modifier.fillParentMaxSize() else Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PendingEventCard(
    event: EventWithMedications,
    onTakenLate: () -> Unit,
    onSkip: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = event.event.icon, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.event.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.pending_yesterday_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Row {
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.skip_action), color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = onTakenLate, shape = MaterialTheme.shapes.small) {
                    Text(stringResource(R.string.take_late_action), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun OnboardingEmptyState(
    paddingValues: PaddingValues,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddAlarm,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_empty_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.testTag("onboarding_add_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_new_time))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_empty_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@CombinedPreviews
@Composable
fun DashboardPreview() {
    MeusRemedinhosTheme {
        DashboardScreen(onNavigateToInventory = {})
    }
}
