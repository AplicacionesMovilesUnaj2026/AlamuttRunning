package com.aplicacionesmoviles.alamutt_running.features.challenges

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.core.data.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

    LaunchedEffect(Unit) { loadData() }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.challenges_title), color = TextWhite, fontWeight = FontWeight.Black, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkerHeader)
                )
            },
            containerColor = DarkBackground
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    item {
                        PointsOverviewCard(points = points)
                    }

                    if (activeChallenges.isNotEmpty()) {
                        item {
                            SectionHeader(title = stringResource(R.string.active_challenges), icon = Icons.AutoMirrored.Filled.DirectionsRun)
                        }
                        items(activeChallenges.toList()) { (distStr, progress) ->
                            val target = distStr.toDoubleOrNull() ?: 1.0
                            ChallengeActiveCard(
                                distance = target,
                                progress = progress,
                                unitSystem = unitSystem,
                                accentColor = AccentRed,
                                cardBg = DarkerHeader,
                                onUnsubscribe = {
                                    scope.launch {
                                        userRepository.unsubscribeFromChallenge(userId, target)
                                        loadData()
                                    }
                                }
                            )
                        }
                    }

                    item {
                        SectionHeader(title = stringResource(R.string.new_challenges), icon = Icons.Default.AddCircleOutline)
                    }
                    items(availableChallenges.filter { !activeChallenges.containsKey(it.toString()) }) { distance ->
                        ChallengeSubscribeCard(
                            distance = distance,
                            unitSystem = unitSystem,
                            accentColor = AccentRed,
                            cardBg = DarkerHeader,
                            onSubscribe = {
                                scope.launch {
                                    userRepository.subscribeToChallenge(userId, distance)
                                    loadData()
                                }
                            }
                        )
                    }

                    if (completedChallenges.isNotEmpty()) {
                        item {
                            SectionHeader(title = stringResource(R.string.your_medals), icon = Icons.Default.EmojiEvents)
                        }
                        items(completedChallenges.reversed()) { completionId ->
                            val parts = completionId.split("-")
                            val dist = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                            val week = parts.getOrNull(2) ?: "?"
                            val year = parts.getOrNull(1) ?: "?"

                            CompletedChallengeCard(
                                distance = dist,
                                week = week,
                                year = year,
                                unitSystem = unitSystem,
                                successColor = successGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PointsOverviewCard(points: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkerHeader),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentRed, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = points.toString(), color = TextWhite, fontSize = 42.sp, fontWeight = FontWeight.Black)
            Text(text = stringResource(R.string.points_lower), color = AccentRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.total_points), color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        Icon(icon, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun ChallengeActiveCard(
    distance: Double,
    progress: Double,
    unitSystem: String,
    accentColor: Color,
    cardBg: Color,
    onUnsubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.runner_badge, UnitConverter.formatDistanceKm(distance, unitSystem)),
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                IconButton(onClick = onUnsubscribe, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.abandon), tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val progressPercent = (progress / distance).coerceIn(0.0, 1.0).toFloat()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TextWhite.copy(alpha = 0.1f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.challenge_progress, UnitConverter.formatDistanceKm(progress, unitSystem), UnitConverter.formatDistanceKm(distance, unitSystem)),
                    color = TextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
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
    cardBg: Color,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        onClick = onSubscribe
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.runner_badge, UnitConverter.formatDistanceKm(distance, unitSystem)),
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = stringResource(R.string.reward_points, (distance * 10).toInt()),
                    color = AccentRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onSubscribe,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(stringResource(R.string.challenge_btn), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CompletedChallengeCard(
    distance: Double,
    week: String,
    year: String,
    unitSystem: String,
    successColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = successColor.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = successColor,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val distFormatted = UnitConverter.formatDistanceKm(distance, unitSystem)
                Text(stringResource(R.string.challenge_card_title, distFormatted), color = successColor, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(stringResource(R.string.challenge_completed_date, week, year), color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}