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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = dateFormatted, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(start = 8.dp))

                StatCardFeatured(title = stringResource(R.string.distance), value = UnitConverter.formatDistance(r.distance, unitSystem))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCardSmall(title = stringResource(R.string.pace), value = "${UnitConverter.formatPace(r.pace, unitSystem)} ${UnitConverter.getPaceUnitLabel(unitSystem)}")
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCardSmall(title = stringResource(R.string.calories), value = r.calories.toString())
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCardSmall(title = stringResource(R.string.steps), value = r.steps.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardFeatured(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkerHeader)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(value, color = AccentRed, fontSize = 48.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StatCardSmall(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkerHeader)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(value, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}
