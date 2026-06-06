package com.aplicacionesmoviles.alamutt_running.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.features.tracking.TrackingViewModel
import com.aplicacionesmoviles.alamutt_running.ui.theme.AccentRed
import com.aplicacionesmoviles.alamutt_running.ui.theme.TextGray
import com.aplicacionesmoviles.alamutt_running.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    trackingViewModel: TrackingViewModel
) {
    val unitSystem by trackingViewModel.unitSystem.collectAsState()
    val countdownTime by trackingViewModel.countdownTime.collectAsState()
    val voiceAlertsEnabled by trackingViewModel.voiceAlertsEnabled.collectAsState()
    val voiceAlertFrequency by trackingViewModel.voiceAlertFrequency.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0E3A6D),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings), color = TextWhite, fontWeight = FontWeight.Black)
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF001B44)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.general),
                color = AccentRed,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.language), color = TextWhite) },
                supportingContent = { Text("Español / English / Português / Русский", color = TextGray) },
                modifier = Modifier.clickable {
                    navController.navigate("language")
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TextGray.copy(alpha = 0.2f))

            Text(
                text = stringResource(R.string.training),
                color = AccentRed,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(stringResource(R.string.units_system), color = TextWhite, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = unitSystem == "Metric",
                        onClick = { trackingViewModel.unitSystem.value = "Metric" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentRed, unselectedColor = TextGray)
                    )
                    Text(stringResource(R.string.metric), color = TextWhite)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = unitSystem == "Imperial",
                        onClick = { trackingViewModel.unitSystem.value = "Imperial" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentRed, unselectedColor = TextGray)
                    )
                    Text(stringResource(R.string.imperial), color = TextWhite)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.voice_alerts), color = TextWhite, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = voiceAlertsEnabled,
                        onCheckedChange = { trackingViewModel.voiceAlertsEnabled.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentRed, checkedTrackColor = AccentRed.copy(alpha = 0.5f))
                    )
                }

                if (voiceAlertsEnabled) {
                    Spacer(Modifier.height(8.dp))
                    val unit = if (unitSystem == "Metric") "km" else "mi"
                    Text(
                        text = stringResource(R.string.frequency, "$voiceAlertFrequency $unit"),
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = voiceAlertFrequency.toFloat(),
                        onValueChange = { trackingViewModel.voiceAlertFrequency.value = it.toDouble() },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = AccentRed, activeTrackColor = AccentRed)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(stringResource(R.string.countdown), color = TextWhite, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    listOf(0, 3, 5, 10).forEach { time ->
                        FilterChip(
                            selected = countdownTime == time,
                            onClick = { trackingViewModel.countdownTime.value = time },
                            label = { Text("${time}s", color = if (countdownTime == time) Color.White else TextWhite) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentRed,
                                containerColor = Color.Transparent,
                                labelColor = TextWhite,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (countdownTime == time) AccentRed else TextGray,
                                enabled = true,
                                selected = countdownTime == time
                            )
                        )
                    }
                }
            }
        }
    }
}
