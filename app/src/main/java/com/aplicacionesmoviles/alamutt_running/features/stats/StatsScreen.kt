package com.aplicacionesmoviles.alamutt_running.features.stats


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = viewModel()
) {
    val unitSystem = viewModel.unitSystem
    val totalRuns = viewModel.totalRuns
    val totalDistanceKm = viewModel.totalDistanceKm
    val totalCalories = viewModel.totalCalories
    val totalSteps = viewModel.totalSteps
    val totalPoints = viewModel.totalPoints
    val rankDistance = viewModel.rankDistance
    val rankCalories = viewModel.rankCalories
    val rankSteps = viewModel.rankSteps
    val rankPoints = viewModel.rankPoints
    val rankRuns = viewModel.rankRuns
    val isLoading = viewModel.isLoading

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
                Text(
                    text = stringResource(R.string.statistics),
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentRed)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        label = stringResource(R.string.points),
                        value = String.format(Locale.US, "%,d", totalPoints),
                        icon = Icons.Default.Star,
                        modifier = Modifier
                            .fillMaxWidth(),
                        rank = rankPoints,
                        isHighlight = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            label = stringResource(R.string.total_distance),
                            value = UnitConverter.formatDistanceKm(totalDistanceKm, unitSystem).split(" ")[0],
                            unit = UnitConverter.getUnitLabel(unitSystem),
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            inlineUnit = true,
                            rank = rankDistance
                        )
                        StatCard(
                            label = stringResource(R.string.runs),
                            value = totalRuns.toString(),
                            icon = Icons.Default.History,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            rank = rankRuns
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            label = stringResource(R.string.calories),
                            value = totalCalories.toString(),
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            rank = rankCalories
                        )
                        StatCard(
                            label = stringResource(R.string.total_steps),
                            value = String.format(Locale.US, "%,d", totalSteps),
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            rank = rankSteps
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    modifier: Modifier = Modifier,
    inlineUnit: Boolean = false,
    rank: Int = 0,
    isHighlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = DarkerHeader
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            if (rank > 0) {
                Surface(
                    color = AccentRed,
                    shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 24.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "#$rank",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isHighlight) 40.dp else 34.dp)
                        .background(AccentRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(if (isHighlight) 24.dp else 18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(if (isHighlight) 4.dp else 2.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = value,
                        fontWeight = FontWeight.Black,
                        fontSize = if (isHighlight) 30.sp else 26.sp,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                    if (unit.isNotEmpty() && inlineUnit) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextGray,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                if (unit.isNotEmpty() && !inlineUnit) {
                    Text(
                        text = unit.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextGray,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}