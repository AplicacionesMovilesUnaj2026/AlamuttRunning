package com.aplicacionesmoviles.alamutt_running.features.run.ui

import com.aplicacionesmoviles.alamutt_running.features.run.viewmodel.RunDetailViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RunDetailScreen(
    runId: String,
    onBack: () -> Unit,
    viewModel: RunDetailViewModel = viewModel()
) {
    val run by viewModel.run.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val unitSystem = viewModel.unitSystem

    LaunchedEffect(runId) {
        viewModel.loadRun(runId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkerHeader)
                .padding(top = 40.dp, start = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
            }
            Text(stringResource(R.string.run_detail), fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextWhite)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentRed)
            }
        } else if (run != null) {
            val r = run!!
            val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(r.date))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = dateFormatted.uppercase(),
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                StatCardFeatured(
                    title = stringResource(R.string.distance),
                    value = UnitConverter.formatDistance(r.distance, unitSystem).split(" ")[0],
                    unit = UnitConverter.getUnitLabel(unitSystem)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCardSmall(
                            title = stringResource(R.string.pace),
                            value = UnitConverter.formatPace(r.pace, unitSystem),
                            unit = UnitConverter.getPaceUnitLabel(unitSystem)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val hours = r.duration / 3600
                        val minutes = (r.duration % 3600) / 60
                        val seconds = r.duration % 60
                        val timeString = if (hours > 0) {
                            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                        } else {
                            String.format(Locale.US, "%02d:%02d", minutes, seconds)
                        }
                        StatCardSmall(title = stringResource(R.string.time), value = timeString)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCardSmall(
                            title = stringResource(R.string.calories),
                            value = r.calories.toString(),
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCardSmall(
                            title = stringResource(R.string.steps),
                            value = String.format(Locale.US, "%,d", r.steps)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardFeatured(title: String, value: String, unit: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkerHeader),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextWhite,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unit.uppercase(),
                    color = TextGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatCardSmall(title: String, value: String, unit: String = "") {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkerHeader),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                color = TextGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit.uppercase(),
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}
