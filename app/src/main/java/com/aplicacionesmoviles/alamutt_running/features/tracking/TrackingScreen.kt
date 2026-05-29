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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aplicacionesmoviles.alamutt_running.model.Run
import com.aplicacionesmoviles.alamutt_running.repository.RunRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TrackingScreen(viewModel: TrackingViewModel, onStop: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val runRepository = remember { RunRepository() }

    val runState by viewModel.runState.collectAsState()
    val timer by viewModel.timerSeconds.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val pace by viewModel.pace.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val steps by viewModel.steps.collectAsState()

    val formattedTime by remember(timer) {
        derivedStateOf {
            val minutes = timer / 60
            val seconds = timer % 60
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    val darkBackground = Color(0xFF0F3460)
    val accentRed = Color(0xFFE94560)
    val lightBlue = Color(0xFF164B85)

    DisposableEffect(Unit) {
        val stepManager = StepCounterManager(context) { count ->
            viewModel.updateSteps(count)
        }
        stepManager.start()
        onDispose {
            stepManager.stop()
            viewModel.resetTracking()
        }
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
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

    Box(Modifier.fillMaxSize().background(darkBackground)) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("TIEMPO", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = formattedTime,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            Text("DISTANCIA", color = Color.Gray, fontSize = 14.sp)
            Text(String.format(Locale.US, "%.2f km", distance / 1000), color = Color.White, fontSize = 32.sp)

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PASOS", color = Color.Gray, fontSize = 14.sp)
                    Text("$steps", color = Color.White, fontSize = 24.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RITMO", color = Color.Gray, fontSize = 14.sp)
                    val pMinutes = pace.toInt()
                    val pSeconds = ((pace - pMinutes) * 60).toInt()
                    Text(String.format(Locale.US, "%d:%02d", pMinutes, pSeconds), color = Color.White, fontSize = 24.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CALORÍAS", color = Color.Gray, fontSize = 14.sp)
                    Text("$calories kcal", color = Color.White, fontSize = 24.sp)
                }
            }

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                            scope.launch {
                                runRepository.saveRun(
                                    Run(
                                        userId = userId,
                                        distance = distance,
                                        pace = pace,
                                        duration = timer,
                                        calories = calories,
                                        steps = steps,
                                        date = System.currentTimeMillis()
                                    )
                                )
                                onStop()
                            }
                        }
                    },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentRed)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Finalizar", tint = Color.White, modifier = Modifier.size(40.dp))
                }

                Spacer(Modifier.width(40.dp))

                Button(
                    onClick = {
                        val newState = if (runState is RunState.Running) RunState.Paused else RunState.Running
                        viewModel.updateRunState(newState)
                    },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = lightBlue)
                ) {
                    Icon(
                        imageVector = if (runState is RunState.Running) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Estado",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}