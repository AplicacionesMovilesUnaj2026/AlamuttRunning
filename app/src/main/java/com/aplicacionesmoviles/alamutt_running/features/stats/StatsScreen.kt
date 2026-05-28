package com.aplicacionesmoviles.alamutt_running.features.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import com.aplicacionesmoviles.alamutt_running.repository.RunRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun StatsScreen(
    navController: NavController
) {

    val runRepository = remember {
        RunRepository()
    }

    var totalRuns by remember {
        mutableIntStateOf(0)
    }

    var totalDistance by remember {
        mutableDoubleStateOf(0.0)
    }

    LaunchedEffect(Unit) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {

            totalRuns = runRepository.getTotalRuns(userId)
            totalDistance = runRepository.getTotalDistance(userId)
        }
    }

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp),
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
                text = "Estadísticas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column(

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Estadísticas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F3460)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {

                        Text(
                            text = "Carreras Totales",
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = totalRuns.toString(),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F3460)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {

                        Text(
                            text = "Distancia Total",
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${totalDistance} km",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

