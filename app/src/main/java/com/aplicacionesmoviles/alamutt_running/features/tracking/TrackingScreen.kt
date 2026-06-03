package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aplicacionesmoviles.alamutt_running.util.UnitConverter
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun TrackingScreen(viewModel: TrackingViewModel, onFinish: (String) -> Unit) {
    val context = LocalContext.current
    val runState by viewModel.runState.collectAsState()
    val timer by viewModel.timerSeconds.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val pace by viewModel.pace.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val completedChallenges by viewModel.completedChallenges.collectAsState()
    val isGoalReached by viewModel.isGoalReached.collectAsState()
    val goalDistance by viewModel.goalDistance.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val formattedTime by remember(timer) {
        derivedStateOf {
            String.format(Locale.US, "%02d:%02d", timer / 60, timer % 60)
        }
    }

    DisposableEffect(Unit) {
        val stepManager = StepCounterManager(context) { count -> viewModel.updateSteps(count) }
        stepManager.start()
        onDispose {
            stepManager.stop()
            viewModel.resetTracking()
        }
    }

    LaunchedEffect(isGoalReached) {
        if (isGoalReached) {
            snackbarHostState.showSnackbar(
                message = "¡Objetivo alcanzado! Sigue así.",
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(completedChallenges) {
        if (completedChallenges.isNotEmpty()) {
            val challengeText = completedChallenges.joinToString(", ") { "${it.toInt()}km" }
            snackbarHostState.showSnackbar(
                message = "¡Desafío completado: $challengeText! +${(completedChallenges.sumOf { it } * 10).toInt()} pts",
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { viewModel.processLocation(it) }
                }
            }
            try {
                client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    Box(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("TIEMPO", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(
                formattedTime,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            Text("DISTANCIA", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(
                UnitConverter.formatDistance(distance, unitSystem),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            if (goalDistance > 0.0) {
                LinearProgressIndicator(
                    progress = { (distance / (goalDistance * 1000.0)).coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${UnitConverter.formatDistance(distance, unitSystem)} / ${UnitConverter.formatDistance(goalDistance * 1000.0, unitSystem)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PASOS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Text(
                        steps.toString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "RITMO",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Text(
                        UnitConverter.formatPace(pace, unitSystem),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "CAL",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Text(
                        calories.toString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                            viewModel.finishAndSaveRun(userId) { runId ->
                                onFinish(runId ?: "CANCELLED")
                            }
                        }
                    },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Finalizar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.width(40.dp))

                Button(
                    onClick = { viewModel.updateRunState(if (runState is RunState.Running) RunState.Paused else RunState.Running) },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = if (runState is RunState.Running) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Estado",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )
    }
}

private fun formatPace(pace: Double): String {
    if (pace <= 0.0 || pace.isNaN() || pace.isInfinite()) {
        return "--:--"
    }

    val minutes = pace.toInt()
    val seconds = ((pace - minutes) * 60).toInt()

    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}