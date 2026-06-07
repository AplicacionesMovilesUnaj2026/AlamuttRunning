package com.aplicacionesmoviles.alamutt_running.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.features.tracking.TrackingViewModel
import com.aplicacionesmoviles.alamutt_running.ui.theme.*
import java.util.Locale

@Composable
fun TrainingSettings(trackingViewModel: TrackingViewModel) {
    val unitSystem by trackingViewModel.unitSystem.collectAsState()
    val countdownTime by trackingViewModel.countdownTime.collectAsState()
    val voiceAlertsEnabled by trackingViewModel.voiceAlertsEnabled.collectAsState()
    val voiceAlertFrequency by trackingViewModel.voiceAlertFrequency.collectAsState()

    Column {
        Text(
            text = stringResource(R.string.units_system),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = unitSystem == "Metric",
                onClick = { trackingViewModel.unitSystem.value = "Metric" },
                colors = RadioButtonDefaults.colors(selectedColor = AccentRed, unselectedColor = TextWhite.copy(alpha = 0.4f))
            )
            Text(stringResource(R.string.metric), color = TextWhite)
            Spacer(Modifier.width(16.dp))
            RadioButton(
                selected = unitSystem == "Imperial",
                onClick = { trackingViewModel.unitSystem.value = "Imperial" },
                colors = RadioButtonDefaults.colors(selectedColor = AccentRed, unselectedColor = TextWhite.copy(alpha = 0.4f))
            )
            Text(stringResource(R.string.imperial), color = TextWhite)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.voice_alerts), color = TextWhite, fontWeight = FontWeight.Bold)
            Switch(
                checked = voiceAlertsEnabled,
                onCheckedChange = { trackingViewModel.voiceAlertsEnabled.value = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextWhite,
                    checkedTrackColor = AccentRed,
                    uncheckedThumbColor = TextWhite.copy(alpha = 0.6f),
                    uncheckedTrackColor = DarkerHeader
                )
            )
        }

        if (voiceAlertsEnabled) {
            Spacer(Modifier.height(8.dp))
            val unit = if (unitSystem == "Metric") stringResource(R.string.unit_km) else stringResource(R.string.unit_mi)
            val formattedFreq = String.format(Locale.US, "%.1f", voiceAlertFrequency)
            
            Text(
                text = stringResource(R.string.frequency, "$formattedFreq $unit"),
                color = TextWhite.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            Slider(
                value = voiceAlertFrequency.toFloat(),
                onValueChange = { trackingViewModel.voiceAlertFrequency.value = it.toDouble() },
                valueRange = 0.5f..5.0f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = AccentRed,
                    activeTrackColor = AccentRed,
                    inactiveTrackColor = DarkerHeader
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // Countdown
        Text(
            text = stringResource(R.string.countdown),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            listOf(0, 3, 5, 10).forEach { time ->
                FilterChip(
                    selected = countdownTime == time,
                    onClick = { trackingViewModel.countdownTime.value = time },
                    label = { Text("${time}s") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentRed,
                        containerColor = DarkerHeader,
                        labelColor = TextWhite,
                        selectedLabelColor = TextWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (countdownTime == time) AccentRed else TextWhite.copy(alpha = 0.2f),
                        enabled = true,
                        selected = countdownTime == time
                    )
                )
            }
        }
    }
}
