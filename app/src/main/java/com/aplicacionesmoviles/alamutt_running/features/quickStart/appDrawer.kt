package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppDrawer(darkBackground: Color, darkerHeader: Color, onLogout: () -> Unit) {
    val context = LocalContext.current
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = darkerHeader
    ) {
        Column(modifier = Modifier.padding(top = 60.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color.Gray) {}
            Text("Usuario", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(top = 8.dp))
        }
        HorizontalDivider(color = darkBackground)
        NavigationDrawerItem(label = { Text("Carrera", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.DirectionsRun, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(label = { Text("Actividad", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.History, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(label = { Text("Desafíos", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.EmojiEvents, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(label = { Text("Bandeja de entrada", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.Email, null, tint = Color.White) }, onClick = {})
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = darkBackground)
        NavigationDrawerItem(label = { Text("Configuración", color = Color.White) }, selected = false, icon = { Icon(Icons.Default.Settings, null, tint = Color.White) }, onClick = {})
        NavigationDrawerItem(
            label = { Text("Cerrar Sesión", color = Color.White) },
            selected = false,
            icon = { Icon(Icons.Default.ExitToApp, null, tint = Color.White) },
            onClick = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show()
                onLogout()
            }
        )
    }
}