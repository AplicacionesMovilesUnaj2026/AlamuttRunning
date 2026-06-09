package com.aplicacionesmoviles.alamutt_running.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.features.tracking.TrackingViewModel
import com.aplicacionesmoviles.alamutt_running.ui.theme.*
import java.util.Locale

@Composable
fun SettingsScreen(
    navController: NavController,
    trackingViewModel: TrackingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkerHeader)
                .padding(top = 40.dp, start = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = TextWhite
                )
            }
            Text(
                text = stringResource(R.string.settings),
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.general),
                color = AccentRed,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.language), color = TextWhite, fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Español / English / Português / Русский", color = TextWhite.copy(alpha = 0.6f)) },
                modifier = Modifier.clickable {
                    navController.navigate("language")
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TextWhite.copy(alpha = 0.1f))

            Text(
                text = stringResource(R.string.training),
                color = AccentRed,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            TrainingSettings(trackingViewModel = trackingViewModel)
        }
    }
}

@Composable
fun SettingsSection(title: String?, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        if (title != null) {
            Text(title, color = TextWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }
        content()
    }
}
