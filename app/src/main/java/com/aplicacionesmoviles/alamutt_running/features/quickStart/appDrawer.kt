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
import com.aplicacionesmoviles.alamutt_running.ui.theme.*
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
    onChallengesClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onCloseDrawer: () -> Unit
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
                .clickable { 
                    onCloseDrawer()
                    onNavigateToProfile() 
                }
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
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = TextWhite,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        HorizontalDivider(color = DarkBackground)

        NavigationDrawerItem(
            label = { Text("Carrera", color = TextWhite, fontWeight = FontWeight.Bold) }, 
            selected = false, 
            icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, tint = TextWhite) }, 
            onClick = { onCloseDrawer() },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )

        NavigationDrawerItem(
            label = { Text("Actividad", color = TextWhite, fontWeight = FontWeight.Bold) },
            selected = false,
            icon = { Icon(Icons.Default.History, null, tint = TextWhite) },
            onClick = { 
                onCloseDrawer()
                onNavigateToHistory() 
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )

        NavigationDrawerItem(
            label = { Text("Estadísticas", color = TextWhite, fontWeight = FontWeight.Bold) },
            selected = false,
            icon = { Icon(Icons.Default.BarChart, null, tint = TextWhite) },
            onClick = {
                onCloseDrawer()
                onStatsClick()
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )

        NavigationDrawerItem(
            label = { Text("Tabla de líderes", color = TextWhite, fontWeight = FontWeight.Bold) },
            selected = false,
            icon = { Icon(Icons.Default.Leaderboard, null, tint = TextWhite) },
            onClick = {
                onCloseDrawer()
                onLeaderboardClick()
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )

        NavigationDrawerItem(
            label = { Text("Desafíos", color = TextWhite, fontWeight = FontWeight.Bold) },
            selected = false,
            icon = { Icon(Icons.Default.EmojiEvents, null, tint = TextWhite) },
            onClick = {
                onCloseDrawer()
                onChallengesClick()
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = DarkBackground)
        NavigationDrawerItem(
            label = { Text("Configuración", color = TextWhite, fontWeight = FontWeight.Bold) }, 
            selected = false, 
            icon = { Icon(Icons.Default.Settings, null, tint = TextWhite) }, 
            onClick = { onCloseDrawer() },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        
        NavigationDrawerItem(
            label = { Text("Cerrar sesión", color = AccentRed, fontWeight = FontWeight.Black) },
            selected = false,
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = AccentRed) },
            onClick = {
                onCloseDrawer()
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show()
                onLogout()
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
    }
}
