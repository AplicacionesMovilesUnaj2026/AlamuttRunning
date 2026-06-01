package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppDrawer(
    darkBackground: Color,
    darkerHeader: Color,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    selectedImage: Uri?,
    profileImageUrl: String?,
    userName: String?,
    onStatsClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
) {
    val context = LocalContext.current
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = darkerHeader
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .clickable { onNavigateToProfile() }
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImage != null || profileImageUrl != null) {
                    AsyncImage(
                        model = selectedImage ?: profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }
            Text(
                text = "Hola, ${userName ?: "Usuario"}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        HorizontalDivider(color = darkBackground)

        NavigationDrawerItem(label = { Text("Carrera", color = Color.White) }, selected = false, icon = { Icon(
            Icons.AutoMirrored.Filled.DirectionsRun, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(label = { Text("Actividad", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.History, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(
            label = { Text("Estadísticas", color = Color.White) },
            selected = false,
            icon = { Icon(Icons.Default.BarChart, null, tint = Color.White) },
            onClick = onStatsClick
        )
        NavigationDrawerItem(label = { Text("Desafíos", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.EmojiEvents, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(
            label = {
                Text("Tabla de líderes", color = Color.White)
            },
            selected = false,
            icon = {
                Icon(
                    Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = {
                onLeaderboardClick()
            }
        )
        NavigationDrawerItem(label = { Text("Bandeja de entrada", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.Email, null, tint = Color.White) }, onClick = {})

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = darkBackground)
        NavigationDrawerItem(label = { Text("Configuración", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.Settings, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(
            label = { Text("Cerrar Sesión", color = Color.White) },
            selected = false,
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White) },
            onClick = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show()
                onLogout()
            }
        )
    }
}