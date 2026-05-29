package com.aplicacionesmoviles.alamutt_running.features.stats

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
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.repository.RunRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun StatsScreen(
    navController: NavController
) {
    val runRepository = remember { RunRepository() }
    val darkBackground = Color(0xFF0F3460)
    val darkerHeader = Color(0xFF0A192F)

    var totalRuns by remember { mutableIntStateOf(0) }
    var totalDistanceKm by remember { mutableDoubleStateOf(0.0) }
    var totalCalories by remember { mutableIntStateOf(0) }
    var totalSteps by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val runs = runRepository.getUserRuns(userId)
            totalRuns = runs.size
            totalDistanceKm = runs.sumOf { it.distance } / 1000.0
            totalCalories = runs.sumOf { it.calories }
            totalSteps = runs.sumOf { it.steps }
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(darkBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkerHeader)
                .padding(top = 40.dp, start = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text("Estadísticas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE94560))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatCard("Carreras Totales", totalRuns.toString())
                StatCard("Distancia Total (km)", "%.2f".format(totalDistanceKm))
                StatCard("Calorías Totales", totalCalories.toString())
                StatCard("Pasos Totales", totalSteps.toString())
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(0.9f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A192F))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = title, color = Color.Gray)
            Text(text = value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}