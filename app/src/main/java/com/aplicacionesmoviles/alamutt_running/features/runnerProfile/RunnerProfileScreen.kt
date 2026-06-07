package com.aplicacionesmoviles.alamutt_running.features.runnerProfile

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnerProfileScreen(
    uid: String,
    navController: NavController,
    viewModel: RunnerProfileViewModel = viewModel()
) {
    val unitSystem = viewModel.unitSystem

    LaunchedEffect(uid) {
        viewModel.loadRunnerProfile(uid)
    }

    val user = viewModel.user

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.runner_profile_title), fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextWhite) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkerHeader
                    )
                )
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentRed)
                } else if (viewModel.error != null) {
                    Text(
                        text = viewModel.error ?: stringResource(R.string.unknown_error),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else if (user != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(DarkerHeader)
                        ) {
                            if (user.photoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = stringResource(R.string.my_profile),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user.name.ifEmpty { stringResource(R.string.no_name) },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )

                        if (user.bio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = user.bio,
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.total_stats).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 2
                        ) {
                            val itemModifier = Modifier.weight(1f).height(130.dp)
                            
                            StatCard(
                                label = stringResource(R.string.distance),
                                value = UnitConverter.formatDistanceKm(user.totalDistance, unitSystem).split(" ")[0],
                                unit = UnitConverter.getUnitLabel(unitSystem),
                                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                                modifier = itemModifier,
                                inlineUnit = true
                            )
                            StatCard(
                                label = stringResource(R.string.runs),
                                value = user.totalRuns.toString(),
                                icon = Icons.Default.History,
                                modifier = itemModifier
                            )
                            StatCard(
                                label = stringResource(R.string.calories),
                                value = user.totalCalories.toString(),
                                unit = stringResource(R.string.kcal),
                                icon = Icons.Default.LocalFireDepartment,
                                modifier = itemModifier
                            )
                            StatCard(
                                label = stringResource(R.string.steps),
                                value = user.totalSteps.toString(),
                                icon = Icons.AutoMirrored.Filled.ShowChart,
                                modifier = itemModifier
                            )
                            StatCard(
                                label = stringResource(R.string.best_pace),
                                value = if (user.bestPace > 0) UnitConverter.formatPace(user.bestPace, unitSystem) else "N/A",
                                unit = if (user.bestPace > 0) UnitConverter.getPaceUnitLabel(unitSystem) else "",
                                icon = Icons.Default.Timer,
                                modifier = itemModifier
                            )
                        }
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
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
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
