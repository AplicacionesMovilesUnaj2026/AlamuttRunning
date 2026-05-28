package com.aplicacionesmoviles.alamutt_running

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aplicacionesmoviles.alamutt_running.features.auth.AuthScreen
import com.aplicacionesmoviles.alamutt_running.features.auth.AuthViewModel
import com.aplicacionesmoviles.alamutt_running.features.quickStart.QuickStartScreen
import com.aplicacionesmoviles.alamutt_running.ui.theme.AlamuttRunningTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.aplicacionesmoviles.alamutt_running.features.stats.StatsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val credentialManager = CredentialManager.create(this)
        val authViewModel = AuthViewModel()
        val auth = FirebaseAuth.getInstance()

        setContent {
            AlamuttRunningTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val startDestination = if (auth.currentUser != null) "quick_start" else "auth"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("auth") {
                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    navController.navigate("quick_start") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onGoogleSignInClick = {
                                    executeGoogleFlux(credentialManager, authViewModel, navController)
                                }
                            )
                        }
                        composable("quick_start") {
                            QuickStartScreen(
                                navController = navController,
                                onStartClick = { },
                                onLogout = {
                                    navController.navigate("auth") {
                                        popUpTo("quick_start") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("stats") {

                            StatsScreen(navController)
                        }
                    }
                }
            }
        }
    }

    private fun executeGoogleFlux(credentialManager: CredentialManager, viewModel: AuthViewModel, navController: androidx.navigation.NavController) {
        val nonce = java.util.UUID.randomUUID().toString()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("426624648889-8ejkg8u8pi41q2me9htchofvfrtne60n.apps.googleusercontent.com")
            .setNonce(nonce)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@MainActivity, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    viewModel.loginWithGoogle(credential.idToken) {
                        navController.navigate("quick_start") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}