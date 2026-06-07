package com.aplicacionesmoviles.alamutt_running.features.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel

data class LeaderboardUser(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val totalDistance: Double = 0.0,
    val totalCalories: Int = 0,
    val totalSteps: Int = 0,
    val points: Int = 0,
    val bestPace: Double = 0.0
)

enum class LeaderboardFilter(val labelRes: Int, val field: String) {
    DISTANCE(R.string.distance, "totalDistance"),
    CALORIES(R.string.calories, "totalCalories"),
    STEPS(R.string.steps, "totalSteps"),
    POINTS(R.string.points, "points"),
    PACE(R.string.pace, "bestPace")
}

@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val unitSystem = viewModel.unitSystem
    val selectedFilter = viewModel.selectedFilter
    val users = viewModel.users
    val isLoading = viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.loadLeaderboard()
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
                .padding(top = 48.dp, start = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
            }
            Text(
                text = stringResource(R.string.leaderboard),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LeaderboardFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.updateFilter(filter) },
                    label = { 
                        Text(
                            text = stringResource(filter.labelRes),
                            fontSize = 11.sp,
                            color = if (selectedFilter == filter) DarkBackground else TextWhite,
                            fontWeight = FontWeight.Black
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentRed,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkerHeader,
                        labelColor = TextWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = TextGray.copy(alpha = 0.3f),
                        selectedBorderColor = AccentRed
                    )
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentRed)
            }
        } else if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_data), color = Color.White.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(users) { index, user ->
                    LeaderboardItem(index, user, selectedFilter, unitSystem, onUserClick = {
                        navController.navigate("runner_profile/${user.uid}")
                    })
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(index: Int, user: LeaderboardUser, filter: LeaderboardFilter, unitSystem: String, onUserClick: () -> Unit) {
    val rankColor = when (index) {
        0 -> Color(0xFFFFD700)
        1 -> Color(0xFFC0C0C0)
        2 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val displayValue = when (filter) {
        LeaderboardFilter.DISTANCE -> UnitConverter.formatDistanceKm(user.totalDistance, unitSystem).removeSuffix(" ${UnitConverter.getUnitLabel(unitSystem)}")
        LeaderboardFilter.CALORIES -> user.totalCalories.toString()
        LeaderboardFilter.STEPS -> user.totalSteps.toString()
        LeaderboardFilter.POINTS -> user.points.toString()
        LeaderboardFilter.PACE -> if (user.bestPace > 0) UnitConverter.formatPace(user.bestPace, unitSystem) else "N/A"
    }
    
    val unitLabel = when (filter) {
        LeaderboardFilter.DISTANCE -> UnitConverter.getUnitLabel(unitSystem)
        LeaderboardFilter.CALORIES -> "kcal"
        LeaderboardFilter.STEPS -> stringResource(R.string.steps_unit)
        LeaderboardFilter.POINTS -> stringResource(R.string.pts)
        LeaderboardFilter.PACE -> UnitConverter.getPaceUnitLabel(unitSystem)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkerHeader
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(rankColor.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> (index + 1).toString()
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = if (index < 3) 18.sp else 14.sp,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))


            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkBackground)
                    .border(1.dp, if (index < 3) rankColor else TextGray.copy(alpha = 0.3f), CircleShape)
            ) {
                if (user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(24.dp),
                        tint = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = user.name.ifEmpty { stringResource(R.string.default_user) },
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = TextWhite,
                maxLines = 1
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = displayValue,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = if (index < 3) rankColor else AccentRed
                )
                Text(
                    text = unitLabel,
                    fontSize = 10.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
