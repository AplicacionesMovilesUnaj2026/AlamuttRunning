package com.aplicacionesmoviles.alamutt_running.features.run.ui

import com.aplicacionesmoviles.alamutt_running.features.run.viewmodel.TrackingViewModel
import com.aplicacionesmoviles.alamutt_running.features.run.components.AppDrawer

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.features.settings.TrainingSettings
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import com.aplicacionesmoviles.alamutt_running.core.data.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import com.aplicacionesmoviles.alamutt_running.features.run.components.MapViewContainer
import com.aplicacionesmoviles.alamutt_running.features.run.viewmodel.MapViewModel

import com.aplicacionesmoviles.alamutt_running.features.auth.AuthViewModel

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun QuickStartScreen(
    navController: NavController,
    trackingViewModel: TrackingViewModel,
    authViewModel: AuthViewModel,
    onStartClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userLocation by viewModel.userLocation.collectAsState()
    val isGpsActive by viewModel.isGpsActive.collectAsState()

    val goalDistance by trackingViewModel.goalDistance.collectAsState()
    val unitSystem by trackingViewModel.unitSystem.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val showNightCard = currentHour !in 6..<20

    val profileImageUrl = authViewModel.userPhotoUrl
    val userName = authViewModel.userName

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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.startTracking(LocationServices.getFusedLocationProviderClient(context), context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setupOsmdroid(context)
        authViewModel.loadUserProfile()

        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.startTracking(LocationServices.getFusedLocationProviderClient(context), context)
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                darkBackground = DarkBackground,
                darkerHeader = DarkerHeader,
                onLogout = onLogout,
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                selectedImage = null,
                profileImageUrl = profileImageUrl,
                userName = userName,
                onStatsClick = {
                    navController.navigate("stats")
                },
                onLeaderboardClick = {
                    navController.navigate("leaderboard")
                },
                onChallengesClick = {
                    navController.navigate("challenges")
                },
                onNavigateToHistory = {
                    onNavigateToHistory()
                },
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)) {

            Box(modifier = Modifier.fillMaxSize()) {
                if (isGpsActive && userLocation != null) {
                    MapViewContainer(
                        userLocation = userLocation!!,
                        mapStyle = "Standard",
                        onMapReady = { viewModel.onMapRenderComplete() }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AccentRed,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                stringResource(R.string.searching_gps),
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.gps_hint),
                                color = TextGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkerHeader)
                        .padding(top = 40.dp, start = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu), tint = TextWhite)
                    }
                    Text(stringResource(R.string.run), color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
                }

                if (isGpsActive && userLocation != null) {
                    if (goalDistance > 0.0) {
                        Card(
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.9f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.goal_prefix, UnitConverter.formatDistance(goalDistance, unitSystem)),
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { trackingViewModel.goalDistance.value = 0.0 },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove), tint = TextWhite.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    if (showNightCard) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkerHeader.copy(alpha = 0.95f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    stringResource(R.string.night_run_title),
                                    fontWeight = FontWeight.Black,
                                    color = AccentRed,
                                    fontSize = 16.sp
                                )
                                Text(
                                    stringResource(R.string.night_run_desc),
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }
            }

            if (isGpsActive && userLocation != null) {
                Button(
                    onClick = { onStartClick() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                        .size(120.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text(stringResource(R.string.start), fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextWhite)
                }

                IconButton(onClick = { showSettingsDialog = true }, modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 150.dp)
                    .offset(x = (-100).dp)
                    .size(60.dp)
                    .background(DarkerHeader, CircleShape)) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings), tint = TextWhite)
                }

                Button(
                    onClick = { showGoalDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkerHeader)
                ) {
                    Text(stringResource(R.string.set_goal), color = TextWhite, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = DarkerHeader,
            titleContentColor = AccentRed,
            textContentColor = TextWhite,
            shape = RoundedCornerShape(16.dp),
            title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Black) },
            text = {
                TrainingSettings(trackingViewModel = trackingViewModel)
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.close), fontWeight = FontWeight.Black)
                }
            }
        )
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = DarkerHeader,
            titleContentColor = AccentRed,
            textContentColor = TextWhite,
            shape = RoundedCornerShape(16.dp),
            title = { Text(stringResource(R.string.goal_dialog_title), fontWeight = FontWeight.Black) },
            text = {
                Column {
                    val unitLabel = if (unitSystem == "Metric") stringResource(R.string.kilometers) else stringResource(R.string.miles)
                    Text(stringResource(R.string.goal_dialog_desc, unitLabel), fontSize = 14.sp, color = TextWhite)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = {
                            if (it.length <= 4) goalInput = it.filter { char -> char.isDigit() || char == '.' }
                        },
                        label = { Text(stringResource(R.string.distance_label, unitLabel), color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = TextGray,
                            focusedLabelColor = AccentRed,
                            cursorColor = AccentRed,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val distanceValue = goalInput.toDoubleOrNull() ?: 0.0
                        val distanceInMeters = if (unitSystem == "Metric") {
                            distanceValue * 1000.0
                        } else {
                            distanceValue / 0.000621371
                        }
                        trackingViewModel.goalDistance.value = distanceInMeters
                        showGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.accept), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text(stringResource(R.string.cancel), color = TextGray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
