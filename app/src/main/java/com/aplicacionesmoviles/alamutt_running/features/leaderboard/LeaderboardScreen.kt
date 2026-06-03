package com.aplicacionesmoviles.alamutt_running.features.leaderboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aplicacionesmoviles.alamutt_running.util.UnitConverter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

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

enum class LeaderboardFilter(val label: String, val field: String) {
    DISTANCE("Distancia", "totalDistance"),
    CALORIES("Calorías", "totalCalories"),
    STEPS("Pasos", "totalSteps"),
    POINTS("Puntos", "points"),
    PACE("Mejor Ritmo", "bestPace")
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 48.dp, start = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = "Tabla de Líderes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        0 -> Color(0xFFFFD700) // Gold
        1 -> Color(0xFFC0C0C0) // Silver
        2 -> Color(0xFFCD7F32) // Bronze
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
        LeaderboardFilter.STEPS -> "pasos"
        LeaderboardFilter.POINTS -> "pts"
        LeaderboardFilter.PACE -> UnitConverter.getPaceUnitLabel(unitSystem)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
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
                    fontWeight = FontWeight.Bold,
                    fontSize = if (index < 3) 18.sp else 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))


            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default Profile",
                        modifier = Modifier.align(Alignment.Center).size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = user.name.ifEmpty { "Usuario" },
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = displayValue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = if (index < 3) rankColor else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = unitLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
