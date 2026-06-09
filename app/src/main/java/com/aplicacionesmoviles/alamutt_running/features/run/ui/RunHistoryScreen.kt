package com.aplicacionesmoviles.alamutt_running.features.run.ui

import com.aplicacionesmoviles.alamutt_running.features.run.viewmodel.RunHistoryViewModel
import com.aplicacionesmoviles.alamutt_running.features.run.components.AppDrawer


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.domain.model.Run
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: RunHistoryViewModel,
    userId: String,
    onBack: () -> Unit,
    onRunClicked: (String) -> Unit
) {
    val history by viewModel.runHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()
    val unitSystem = viewModel.unitSystem

    val reachedBottom: Boolean by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            totalItems > 0 && lastVisibleItem != null && lastVisibleItem.index >= totalItems - 1
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom && !isLoading && userId.isNotEmpty()) {
            viewModel.loadMoreRuns(userId)
        }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            android.util.Log.d("HistoryScreen", "Initializing history for userId: $userId")
            viewModel.loadMoreRuns(userId)
        }
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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }
            Text(
                text = stringResource(R.string.activity),
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        if (!isLoading && history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_runs),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { run ->
                    RunItem(run = run, unitSystem = unitSystem, onClick = { onRunClicked(run.id) })
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RunItem(run: Run, unitSystem: String, onClick: () -> Unit) {
    val dateFormatted = try {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(run.date))
    } catch (e: Exception) { stringResource(R.string.date_unavailable) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkerHeader
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateFormatted.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = UnitConverter.formatDistance(run.distance, unitSystem).split(" ")[0],
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = UnitConverter.getUnitLabel(unitSystem).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                val paceValue = UnitConverter.formatPace(run.pace, unitSystem)
                val paceUnit = if (unitSystem == "Metric") stringResource(R.string.unit_pace_metric) else stringResource(R.string.unit_pace_imperial)
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = paceValue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = paceUnit.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}
