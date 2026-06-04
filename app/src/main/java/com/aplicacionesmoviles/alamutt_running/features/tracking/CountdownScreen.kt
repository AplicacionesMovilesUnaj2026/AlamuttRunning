package com.aplicacionesmoviles.alamutt_running.features.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun CountdownScreen(
    viewModel: TrackingViewModel,
    navController: NavController
) {
    val initialCount by viewModel.countdownTime.collectAsState()
    var count by remember { mutableIntStateOf(initialCount) }

    LaunchedEffect(Unit) {
        if (initialCount == 0) {
            viewModel.updateRunState(RunState.Running)
            navController.navigate("tracking") {
                popUpTo("countdown") { inclusive = true }
            }
            return@LaunchedEffect
        }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 0) count.toString() else "¡YA!",
            fontSize = 120.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
    }
}