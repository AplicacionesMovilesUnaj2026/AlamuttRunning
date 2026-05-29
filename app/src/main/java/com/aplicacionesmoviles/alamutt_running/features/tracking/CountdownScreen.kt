package com.aplicacionesmoviles.alamutt_running.features.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CountdownScreen(
    viewModel: TrackingViewModel,
    navController: androidx.navigation.NavController
) {
    var count by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        viewModel.updateRunState(RunState.Countdown)
        while (count > 0) {
            delay(1000)
            count--
        }
        viewModel.updateRunState(RunState.Running)

        navController.navigate("tracking") {
            popUpTo("countdown") { inclusive = true }
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0F3460)), contentAlignment = Alignment.Center) {
        Text(if (count > 0) count.toString() else "¡YA!", fontSize = 80.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}