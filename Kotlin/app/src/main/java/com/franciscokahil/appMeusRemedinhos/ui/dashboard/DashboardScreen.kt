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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.background.AlarmSchedulerImpl
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    highlightedId: String? = null,
    onHighlightedConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { EventRepositoryImpl(context, database.eventDao()) }
    val alarmScheduler = remember { AlarmSchedulerImpl(context) }
    val factory = remember { DashboardViewModelFactory(repository, alarmScheduler) }
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    
    val events by viewModel.events.collectAsState()
    val shouldShowOnboarding by viewModel.shouldShowOnboarding.collectAsState()
    var expandedEventId by remember { mutableStateOf<String?>(null) }
    
    // FAB Menu State
    var isFabExpanded by remember { mutableStateOf(false) }
    var pendingNewEvent by remember { mutableStateOf<EventEntity?>(null) }
    
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
            viewModel.addEvent(params.title, params.time, params.icon, params.medications)
        }
        pendingNewEvent = null
        expandedEventId = null
    }

    val listState = rememberLazyListState()
    var activeHighlightId by remember { mutableStateOf<String?>(null) }

    // Scroll to highlighted item from widget deep-link
    LaunchedEffect(highlightedId, events) {
        if (highlightedId != null && events.isNotEmpty()) {
            // Auto-collapse any expanded card when navigating via deep-link
            expandedEventId = null

            val index = events.indexOfFirst { it.id == highlightedId }
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

    val createNewEvent = { label: String, time: String, icon: String?, meds: List<Medication> ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                viewModel.addEvent(label, time, icon, meds)
                expandedEventId = null
                pendingNewEvent = null
            } else {
                pendingNewEvent = EventEntity(
                    id = "NEW_EVENT",
                    title = label,
                    time = time,
                    icon = icon ?: "access_time",
                    medications = meds
                )
                showPermissionExplanation = true
            }
        } else {
            viewModel.addEvent(label, time, icon, meds)
            expandedEventId = null
            pendingNewEvent = null
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
                        pendingNewEvent = EventEntity(
                            id = "NEW_EVENT",
                            title = preset?.label ?: "",
                            time = preset?.time ?: "12:00",
                            icon = preset?.icon ?: "access_time"
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
        
        if (showPermissionExplanation) {
            AlertDialog(
                onDismissRequest = { 
                    showPermissionExplanation = false
                    // Proceed anyway
                    pendingNewEvent?.let { params ->
                        viewModel.addEvent(params.title, params.time, params.icon, params.medications)
                    }
                    pendingNewEvent = null
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
                    }) {
                        Text(stringResource(R.string.permission_dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showPermissionExplanation = false
                        pendingNewEvent?.let { params ->
                            viewModel.addEvent(params.title, params.time, params.icon, params.medications)
                        }
                        pendingNewEvent = null
                        expandedEventId = null
                    }) {
                        Text(stringResource(R.string.permission_dialog_cancel))
                    }
                }
            )
        }

        if (events.isEmpty() && expandedEventId != "NEW_EVENT") {
            OnboardingEmptyState(
                paddingValues = paddingValues,
                onAddClick = { isFabExpanded = true },
                modifier = Modifier.testTag("empty_state")
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("event_list"),
                verticalArrangement = if (expandedEventId == null) Arrangement.spacedBy(16.dp) else Arrangement.Top,
                contentPadding = if (expandedEventId == null) PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 8.dp) else PaddingValues(0.dp)
            ) {
                if (expandedEventId == "NEW_EVENT" && pendingNewEvent != null) {
                    item(key = "NEW_EVENT") {
                        EventCard(
                            event = pendingNewEvent!!,
                            isExpanded = true,
                            onExpandClick = {
                                expandedEventId = null
                                pendingNewEvent = null
                            },
                            onSave = { title, time, meds ->
                                createNewEvent(title, time, pendingNewEvent!!.icon, meds)
                            },
                            onDelete = { },
                            onToggleTaken = { },
                            modifier = Modifier.fillParentMaxSize(),
                            isNewEvent = true
                        )
                    }
                }

                items(events, key = { it.id }) { event ->
                    val isHighlighted = activeHighlightId == event.id
                    val isExpanded = expandedEventId == event.id
                    
                    val highlightColor by animateColorAsState(
                        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                        animationSpec = tween(500), label = "highlight"
                    )

                    if (expandedEventId == null || isExpanded) {
                        EventCard(
                            event = event,
                            isExpanded = isExpanded,
                            onExpandClick = {
                                expandedEventId = if (isExpanded) null else event.id
                            },
                            onSave = { title, time, meds ->
                                viewModel.updateEvent(event, title, time, meds)
                                expandedEventId = null
                            },
                            onDelete = {
                                viewModel.deleteEvent(event)
                                expandedEventId = null
                            },
                            onToggleTaken = { isTaken ->
                                viewModel.toggleEventStatus(event, isTaken)
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
            shape = MaterialTheme.shapes.medium
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

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MeusRemedinhosTheme {
        DashboardScreen()
    }
}
