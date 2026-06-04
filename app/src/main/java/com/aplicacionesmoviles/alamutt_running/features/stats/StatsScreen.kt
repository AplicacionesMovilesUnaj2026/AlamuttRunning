package com.aplicacionesmoviles.alamutt_running.features.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.ui.theme.*
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
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Estadísticas",
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
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 2
                    ) {
                        val itemModifier = Modifier.weight(1f).height(125.dp)

                        StatCard(
                            label = "Distancia Total",
                            value = UnitConverter.formatDistanceKm(totalDistanceKm, unitSystem).split(" ")[0],
                            unit = UnitConverter.getUnitLabel(unitSystem),
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            modifier = itemModifier,
                            inlineUnit = true
                        )
                        StatCard(
                            label = "Carreras",
                            value = totalRuns.toString(),
                            icon = Icons.Default.History,
                            modifier = itemModifier
                        )
                        StatCard(
                            label = "Calorías",
                            value = totalCalories.toString(),
                            unit = "kcal",
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = itemModifier
                        )
                        StatCard(
                            label = "Pasos Totales",
                            value = String.format(Locale.US, "%,d", totalSteps),
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            modifier = itemModifier
                        )
                    }
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
    inlineUnit: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = DarkerHeader
        ),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = TextWhite
                )
                if (unit.isNotEmpty() && inlineUnit) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            if (unit.isNotEmpty() && !inlineUnit) {
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray
            )
        }
    }
}
