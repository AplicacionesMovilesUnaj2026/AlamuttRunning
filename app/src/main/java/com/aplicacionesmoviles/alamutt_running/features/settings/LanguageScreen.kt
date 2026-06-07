package com.aplicacionesmoviles.alamutt_running.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.aplicacionesmoviles.alamutt_running.R
import com.aplicacionesmoviles.alamutt_running.ui.theme.*

@Composable
fun LanguageScreen(
    navController: NavController
) {
    val context = LocalContext.current

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
                text = stringResource(R.string.language),
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            LanguageItem("Español 🇪🇸") { LanguageManager.setLanguage(context, "es") }
            HorizontalDivider(color = TextWhite.copy(alpha = 0.1f))
            LanguageItem("English 🇺🇸") { LanguageManager.setLanguage(context, "en") }
            HorizontalDivider(color = TextWhite.copy(alpha = 0.1f))
            LanguageItem("Português 🇧🇷") { LanguageManager.setLanguage(context, "pt") }
            HorizontalDivider(color = TextWhite.copy(alpha = 0.1f))
            LanguageItem("Русский 🇷🇺") { LanguageManager.setLanguage(context, "ru") }
        }
    }
}

@Composable
fun LanguageItem(name: String, onClick: () -> Unit) {
    Text(
        text = name,
        color = TextWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp)
    )
}
