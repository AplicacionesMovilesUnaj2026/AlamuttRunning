package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.data.cloudinary.CloudinaryRepository
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun QuickStartScreen(
    navController: NavController,
    onStartClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userLocation by viewModel.userLocation.collectAsState()
    val isMapFullyRendered by viewModel.isMapFullyRendered.collectAsState()

    val darkBackground = Color(0xFF0F3460)
    val darkerHeader = Color(0xFF0A192F)
    val accentRed = Color(0xFFE94560)
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val showNightCard = currentHour >= 20

    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }

    val cloudinaryRepository = remember { CloudinaryRepository() }
    val userRepository = remember { UserRepository() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (locationGranted) {
            viewModel.startTracking(LocationServices.getFusedLocationProviderClient(context))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setupOsmdroid(context)

        val permissionsToRequest = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.startTracking(LocationServices.getFusedLocationProviderClient(context))
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        FirebaseAuth.getInstance().currentUser?.let { user ->
            scope.launch {
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
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                darkBackground = darkBackground,
                darkerHeader = darkerHeader,
                onLogout = onLogout,
                onNavigateToProfile = {
                    scope.launch { drawerState.close() }
                    navController.navigate("profile")
                },
                selectedImage = selectedImage,
                profileImageUrl = profileImageUrl,
                userName = userName,
                onStatsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("stats")
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(darkBackground)) {
            if (userLocation != null) {
                MapViewContainer(userLocation = userLocation!!, onMapReady = { viewModel.onMapRenderComplete() })
            }

            Row(modifier = Modifier.fillMaxWidth().background(darkerHeader).padding(top = 40.dp, start = 16.dp, bottom = 16.dp).align(Alignment.TopCenter), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White) }
                Text("Carrera", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }

            if (userLocation == null || !isMapFullyRendered) {
                Box(modifier = Modifier.fillMaxSize().background(darkBackground), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentRed)
                }
            } else {
                if (showNightCard) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 120.dp, start = 16.dp, end = 16.dp).align(Alignment.TopCenter),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("¿Corres en la oscuridad?", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Lleva una luz por motivos de seguridad.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Button(
                    onClick = { onStartClick() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                        .size(120.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = accentRed)
                ) {
                    Text("COMENZAR", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                }

                IconButton(onClick = {}, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 150.dp).offset(x = (-100).dp).size(60.dp).background(darkerHeader, CircleShape)) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
                }

                Button(onClick = {}, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = darkerHeader)) {
                    Text("Establece un objetivo", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}