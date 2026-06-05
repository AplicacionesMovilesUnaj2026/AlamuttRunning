package com.aplicacionesmoviles.alamutt_running.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    navController: NavController
) {

    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFF0E3A6D),

        topBar = {
            TopAppBar(
                title = {
                    Text("Language")
                },

                navigationIcon = {
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
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF001B44)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {

            Text(
                text = "Español 🇪🇸",
                modifier = Modifier.clickable {
                    LanguageManager.setLanguage(context, "es")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "English 🇺🇸",
                modifier = Modifier.clickable {
                    LanguageManager.setLanguage(context, "en")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Português 🇧🇷",
                modifier = Modifier.clickable {
                    LanguageManager.setLanguage(context, "pt")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Русский 🇷🇺",
                modifier = Modifier.clickable {
                    LanguageManager.setLanguage(context, "ru")
                }
            )
        }
    }
}