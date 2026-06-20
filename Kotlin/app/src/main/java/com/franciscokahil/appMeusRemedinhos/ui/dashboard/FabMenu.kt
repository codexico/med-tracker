package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme

data class PresetOption(
    val label: String,
    val time: String,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOptionSelected: (PresetOption?) -> Unit,
    tooltipState: TooltipState,
    tooltipOffsetX: Float,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(if (isExpanded) 45f else 0f, label = "rotation")
    
    val presets = listOf(
        PresetOption(stringResource(R.string.wake_up), "07:00", "🕐"),
        PresetOption(stringResource(R.string.breakfast), "08:00", "🍳"),
        PresetOption(stringResource(R.string.morning), "10:00", "☀️"),
        PresetOption(stringResource(R.string.lunch), "12:00", "🍽️"),
        PresetOption(stringResource(R.string.afternoon), "15:00", "🌤️"),
        PresetOption(stringResource(R.string.dinner), "20:00", "🍴"),
        PresetOption(stringResource(R.string.sleep), "22:00", "🌙"),
        PresetOption(stringResource(R.string.preset_other), "12:00", "⏰")
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                positioning = TooltipAnchorPosition.Left
            ),
            tooltip = {
                PlainTooltip(
                    containerColor = MaterialTheme.colorScheme.primary, // Using primary for tooltip
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    caretShape = TooltipDefaults.caretShape(),
                    modifier = Modifier.offset { IntOffset(tooltipOffsetX.dp.roundToPx() - 30, 0) }
                ) {
                    Text(
                        text = stringResource(
                            if (isExpanded) R.string.onboarding_fab_menu_tooltip 
                            else R.string.onboarding_fab_tooltip
                        ),
                        textAlign = TextAlign.End
                    )
                }
            },
            state = tooltipState
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp) // Slightly more space
                    ) {
                        presets.forEach { preset ->
                            PresetFabItem(preset) { onOptionSelected(preset) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                FloatingActionButton(
                    onClick = onToggle,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = stringResource(if (isExpanded) R.string.cancel else R.string.add_new_time),
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    }
}

@Composable
fun PresetFabItem(
    preset: PresetOption,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primaryContainer, // High contrast container
        shape = CircleShape,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = preset.icon, fontSize = 20.sp)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = preset.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FabMenuExpandedPreview() {
    MeusRemedinhosTheme {
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
            FabMenu(
                isExpanded = true,
                onToggle = {},
                onOptionSelected = {},
                tooltipState = rememberTooltipState(initialIsVisible = false),
                tooltipOffsetX = 0f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FabMenuInteractivePreview() {
    MeusRemedinhosTheme {
        var isExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
            FabMenu(
                isExpanded = isExpanded,
                onToggle = { isExpanded = !isExpanded },
                onOptionSelected = {},
                tooltipState = rememberTooltipState(initialIsVisible = false),
                tooltipOffsetX = 0f
            )
        }
    }
}
