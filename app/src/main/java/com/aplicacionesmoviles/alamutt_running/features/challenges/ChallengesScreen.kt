package com.aplicacionesmoviles.alamutt_running.features.challenges

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.util.UnitConverter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val prefs = remember { context.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE) }
    val unitSystem = remember { prefs.getString("unit_system", "Metric") ?: "Metric" }
    
    var activeChallenges by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var completedChallenges by remember { mutableStateOf<List<String>>(emptyList()) }
    var points by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val darkBackground = Color(0xFF0F3460)
    val darkerHeader = Color(0xFF1A1A2E)
    val accentRed = Color(0xFFE94560)
    val successGreen = Color(0xFF4ECCA3)

    val availableChallenges = listOf(5.0, 10.0, 21.0, 42.0)

    fun loadData() {
        scope.launch {
            val userData = userRepository.getUserData(userId)
            activeChallenges = (userData?.get("activeChallenges") as? Map<String, Any>)?.mapValues { 
                (it.value as? Number)?.toDouble() ?: 0.0 
            } ?: emptyMap()
            completedChallenges = (userData?.get("completedChallenges") as? List<String>) ?: emptyList()
            points = (userData?.get("points") as? Long)?.toInt() ?: 0
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Desafíos", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkerHeader)
            )
        },
        containerColor = darkBackground
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111D35)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Puntos Acumulados",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "$points",
                                color = Color.White,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "PUNTOS",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                if (activeChallenges.isNotEmpty()) {
                    item {
                        Text("Desafíos en curso", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    items(activeChallenges.toList()) { (distStr, progress) ->
                        val target = distStr.toDoubleOrNull() ?: 1.0
                        ChallengeActiveCard(
                            distance = target,
                            progress = progress,
                            unitSystem = unitSystem,
                            accentColor = accentRed,
                            onUnsubscribe = {
                                val currentChallenges = activeChallenges
                                activeChallenges = activeChallenges.filterKeys { it != distStr }
                                scope.launch {
                                    try {
                                        userRepository.unsubscribeFromChallenge(userId, target)
                                        loadData()
                                    } catch (e: Exception) {
                                        activeChallenges = currentChallenges
                                    }
                                }
                            }
                        )
                    }
                }

                item {
                    Text("Suscribirse a nuevos retos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                items(availableChallenges.filter { !activeChallenges.containsKey(it.toString()) }) { distance ->
                    ChallengeSubscribeCard(
                        distance = distance,
                        unitSystem = unitSystem,
                        accentColor = accentRed,
                        onSubscribe = {
                            val distKey = distance.toString()
                            val currentChallenges = activeChallenges
                            activeChallenges = activeChallenges + (distKey to 0.0)
                            scope.launch {
                                try {
                                    userRepository.subscribeToChallenge(userId, distance)
                                    loadData()
                                } catch (e: Exception) {
                                    activeChallenges = currentChallenges
                                }
                            }
                        }
                    )
                }

                if (completedChallenges.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Historial de Logros", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(completedChallenges.reversed()) { completionId ->
                        val parts = completionId.split("-")
                        val dist = parts.getOrNull(0) ?: "?"
                        val week = parts.getOrNull(2) ?: "?"
                        val year = parts.getOrNull(1) ?: "?"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    val formattedChallengeDist = UnitConverter.formatDistanceKm(dist.toDoubleOrNull() ?: 0.0, unitSystem)
                                    Text("Desafío $formattedChallengeDist Completado", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Semana $week, $year", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeActiveCard(
    distance: Double,
    progress: Double,
    unitSystem: String,
    accentColor: Color,
    onUnsubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${UnitConverter.formatDistanceKm(distance, unitSystem)} Runner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onUnsubscribe) {
                    Icon(Icons.Default.Close, contentDescription = "Abandonar", tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            val progressPercent = (progress / distance).coerceIn(0.0, 1.0).toFloat()
            
            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = accentColor,
                trackColor = Color.White.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${UnitConverter.formatDistanceKm(progress, unitSystem)} / ${UnitConverter.formatDistanceKm(distance, unitSystem)}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ChallengeSubscribeCard(
    distance: Double,
    unitSystem: String,
    accentColor: Color,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
        shape = RoundedCornerShape(16.dp),
        onClick = onSubscribe
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${UnitConverter.formatDistanceKm(distance, unitSystem)} Runner", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("Recompensa: ${(distance * 10).toInt()} puntos", color = Color.Gray, fontSize = 13.sp)
            }
            
            Button(
                onClick = onSubscribe,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Retar", fontWeight = FontWeight.Bold)
            }
        }
    }
}
