package com.aplicacionesmoviles.alamutt_running.features.run.ui

import com.aplicacionesmoviles.alamutt_running.features.run.ui.RunState
import com.aplicacionesmoviles.alamutt_running.features.run.viewmodel.TrackingViewModel
import com.aplicacionesmoviles.alamutt_running.features.run.viewmodel.MapViewModel
import com.aplicacionesmoviles.alamutt_running.features.run.components.AppDrawer
import com.aplicacionesmoviles.alamutt_running.features.run.components.MapViewContainer
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import com.aplicacionesmoviles.alamutt_running.features.run.data.StepCounterManager
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun TrackingScreen(viewModel: TrackingViewModel, onFinish: (String) -> Unit) {
    val context = LocalContext.current

    BackHandler(enabled = true) {

    }

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
    val gpsLost by viewModel.gpsLost.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val formattedTime by remember(timer) {
        derivedStateOf {
            String.format(Locale.US, "%02d:%02d", timer / 60, timer % 60)
        }
    }

    DisposableEffect(context) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            if (originalOrientation != null) {
                activity.requestedOrientation = originalOrientation
            }
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
                message = context.getString(R.string.goal_reached),
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(completedChallenges) {
        if (completedChallenges.isNotEmpty()) {
            val challengeText = completedChallenges.joinToString(", ") { "${it.toInt()}km" }
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.challenge_completed, challengeText, (completedChallenges.sumOf { it } * 10).toInt()),
                duration = SnackbarDuration.Long
            )
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(Modifier
            .fillMaxSize()
            .background(DarkBackground)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.time), color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    formattedTime,
                    color = TextWhite,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(24.dp))

                Text(stringResource(R.string.distance), color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                val distanceText = if (goalDistance > 0.0) {
                    "${UnitConverter.formatDistance(distance, unitSystem)} / ${UnitConverter.formatDistance(goalDistance, unitSystem)}"
                } else {
                    UnitConverter.formatDistance(distance, unitSystem)
                }
                Text(
                    text = distanceText,
                    color = if (goalDistance > 0.0) AccentRed else TextWhite,
                    fontSize = if (goalDistance > 0.0) 36.sp else 48.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.steps),
                            color = TextGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            steps.toString(),
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.pace),
                            color = TextGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            UnitConverter.formatPace(pace, unitSystem),
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.calories),
                            color = TextGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            calories.toString(),
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(Modifier.height(56.dp))

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
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = stringResource(R.string.finish),
                            tint = TextWhite,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.width(40.dp))

                    Button(
                        onClick = { viewModel.updateRunState(if (runState is RunState.Running) RunState.Paused else RunState.Running) },
                        modifier = Modifier.size(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkerHeader)
                    ) {
                        Icon(
                            imageVector = if (runState is RunState.Running) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.run_status),
                            tint = TextWhite,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            // GPS loss banner — shown at top, does not cover controls at bottom.
            // Auto-dismissed when GPS recovers (gpsLost returns to false).
            if (gpsLost) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0xFFFFC107) // amber/warning
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.gps_signal_lost),
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DarkerHeader,
                    contentColor = TextWhite,
                    actionColor = AccentRed,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
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
