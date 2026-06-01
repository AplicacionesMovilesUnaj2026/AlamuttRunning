package com.aplicacionesmoviles.alamutt_running.features.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class LeaderboardUser(
    val name: String = "",
    val totalDistance: Double = 0.0
)

@Composable
fun LeaderboardScreen(
    navController: NavController
) {

    var users by remember {
        mutableStateOf<List<LeaderboardUser>>(emptyList())
    }

    LaunchedEffect(Unit) {

        val result = FirebaseFirestore
            .getInstance()
            .collection("users")
            .orderBy("totalDistance")
            .get()
            .await()

        users = result.documents.map {

            LeaderboardUser(
                name = it.getString("name") ?: "Usuario",
                totalDistance = it.getDouble("totalDistance") ?: 0.0
            )

        }.sortedByDescending {
            it.totalDistance
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {

                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null
                )
            }

            Text(
                text = "Tabla de líderes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {

            itemsIndexed(users) { index, user ->

                val medal = when(index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> "#${index + 1}"
                }

                val cardColor = when(index) {
                    0 -> Color(0xFFFFD700)
                    1 -> Color(0xFFC0C0C0)
                    2 -> Color(0xFFCD7F32)
                    else -> Color(0xFF0F3460)
                }

                val textColor =
                    if (index <= 2)
                        Color.Black
                    else
                        Color.White

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = medal,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {

                            Text(
                                text = user.name,
                                color = Color.White
                            )

                            Text(
                                text = "${user.totalDistance} km",
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}