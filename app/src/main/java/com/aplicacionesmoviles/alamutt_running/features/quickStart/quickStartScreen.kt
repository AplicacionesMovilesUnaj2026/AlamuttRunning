package com.aplicacionesmoviles.alamutt_running.features.quickStart

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.aplicacionesmoviles.alamutt_running.features.tracking.TrackingViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.ui.theme.*
import com.aplicacionesmoviles.alamutt_running.util.UnitConverter
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun QuickStartScreen(
    navController: NavController,
    trackingViewModel: TrackingViewModel,
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
    val countdownTime by trackingViewModel.countdownTime.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val showNightCard = currentHour >= 20

    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }

    val userRepository = remember { UserRepository() }

    // --- BLOQUEO DE ORIENTACIÓN VERTICAL ---
    DisposableEffect(context) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation

        // Forzamos vertical fija
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {
            // Al salir de la pantalla, restauramos la orientación previa
            if (originalOrientation != null) {
                activity.requestedOrientation = originalOrientation
            }
        }
    }
    // ----------------------------------------

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.startTracking(LocationServices.getFusedLocationProviderClient(context), context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setupOsmdroid(context)

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

        FirebaseAuth.getInstance().currentUser?.let { user ->
            val userData = userRepository.getUserData(user.uid)
            val dbName = userData?.get("name") as? String
            val photo = userData?.get("photoUrl") as? String

            userName = dbName.takeIf { !it.isNullOrBlank() }
                ?: user.displayName?.takeIf { it.isNotBlank() }
                        ?: "Usuario"
            profileImageUrl = photo.takeIf { !it.isNullOrBlank() }
                ?: user.photoUrl?.toString()
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
                                "Buscando señal GPS...",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Asegúrate de estar en un lugar despejado",
                                color = TextGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .background(DarkerHeader)
                .padding(top = 40.dp, start = 16.dp, bottom = 16.dp)
                .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú", tint = TextWhite)
                }
                Text("Carrera", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
            }

            if (goalDistance > 0.0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 110.dp),
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
                            text = "Objetivo: ${UnitConverter.formatDistance(goalDistance, unitSystem)}",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { trackingViewModel.goalDistance.value = 0.0 },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar", tint = TextWhite.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            if (showNightCard && userLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 160.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkerHeader)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "¿Corres en la oscuridad?",
                            fontWeight = FontWeight.Black,
                            color = AccentRed,
                            fontSize = 16.sp
                        )
                        Text(
                            "Lleva una luz por motivos de seguridad.",
                            fontSize = 13.sp,
                            color = TextGray
                        )
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
                    Text("Comenzar", fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextWhite)
                }

                IconButton(onClick = { showSettingsDialog = true }, modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 150.dp)
                    .offset(x = (-100).dp)
                    .size(60.dp)
                    .background(DarkerHeader, CircleShape)) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = TextWhite)
                }

                Button(
                    onClick = { showGoalDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkerHeader)
                ) {
                    Text("Establece un objetivo", color = TextWhite, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp))
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
            title = { Text("Configuración", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Sistema de unidades", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentRed)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = unitSystem == "Metric",
                            onClick = { trackingViewModel.unitSystem.value = "Metric" },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentRed, unselectedColor = TextGray)
                        )
                        Text("Métrico (km)", color = TextWhite)
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = unitSystem == "Imperial",
                            onClick = { trackingViewModel.unitSystem.value = "Imperial" },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentRed, unselectedColor = TextGray)
                        )
                        Text("Imperial (mi)", color = TextWhite)
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = TextGray.copy(alpha = 0.3f))

                    Text("Cuenta regresiva", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentRed)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        listOf(0, 3, 5, 10).forEach { time ->
                            FilterChip(
                                selected = countdownTime == time,
                                onClick = { trackingViewModel.countdownTime.value = time },
                                label = { Text("${time}s", color = if (countdownTime == time) DarkBackground else TextWhite) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentRed,
                                    containerColor = DarkBackground,
                                    labelColor = TextWhite,
                                    selectedLabelColor = DarkBackground
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (countdownTime == time) AccentRed else TextGray,
                                    enabled = true,
                                    selected = countdownTime == time
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Black)
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
            title = { Text("¿Cuál es tu objetivo?", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    val unitLabel = if (unitSystem == "Metric") "kilómetros" else "millas"
                    Text("Ingresa la distancia en $unitLabel que deseas recorrer hoy.", fontSize = 14.sp, color = TextWhite)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = {
                            if (it.length <= 4) goalInput = it.filter { char -> char.isDigit() || char == '.' }
                        },
                        label = { Text("Distancia ($unitLabel)", color = TextGray) },
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
                    Text("Aceptar", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancelar", color = TextGray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}