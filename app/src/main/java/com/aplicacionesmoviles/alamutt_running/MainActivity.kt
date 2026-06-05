package com.aplicacionesmoviles.alamutt_running

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aplicacionesmoviles.alamutt_running.features.RunDetail.RunDetailScreen
import com.aplicacionesmoviles.alamutt_running.features.RunDetail.RunDetailViewModel
import com.aplicacionesmoviles.alamutt_running.features.RunHistory.HistoryScreen
import com.aplicacionesmoviles.alamutt_running.features.RunHistory.RunHistoryViewModel
import com.aplicacionesmoviles.alamutt_running.features.auth.AuthScreen
import com.aplicacionesmoviles.alamutt_running.features.auth.AuthViewModel
import com.aplicacionesmoviles.alamutt_running.features.challenges.ChallengesScreen
import com.aplicacionesmoviles.alamutt_running.features.leaderboard.LeaderboardScreen
import com.aplicacionesmoviles.alamutt_running.features.onboarding.OnboardingScreen
import com.aplicacionesmoviles.alamutt_running.features.profile.ProfileScreen
import com.aplicacionesmoviles.alamutt_running.features.runnerProfile.RunnerProfileScreen
import com.aplicacionesmoviles.alamutt_running.features.quickStart.QuickStartScreen
import com.aplicacionesmoviles.alamutt_running.features.stats.StatsScreen
import com.aplicacionesmoviles.alamutt_running.features.tracking.CountdownScreen
import com.aplicacionesmoviles.alamutt_running.features.tracking.TrackingScreen
import com.aplicacionesmoviles.alamutt_running.features.tracking.TrackingViewModel
import com.aplicacionesmoviles.alamutt_running.ui.theme.AlamuttRunningTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.aplicacionesmoviles.alamutt_running.features.settings.SettingsScreen
import com.aplicacionesmoviles.alamutt_running.features.settings.LanguageScreen
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MainActivity : ComponentActivity() {

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("426624648889-8ejkg8u8pi41q2me9htchofvfrtne60n.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    private var sharedAuthViewModel: AuthViewModel? = null
    private lateinit var navController: NavHostController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Las notificaciones son necesarias", Toast.LENGTH_LONG).show()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    sharedAuthViewModel?.loginWithGoogle(idToken) { shouldGoToOnboarding ->
                        if (shouldGoToOnboarding) {
                            navController.navigate("onboarding") { popUpTo("auth") { inclusive = true } }
                        } else {
                            navController.navigate("quick_start") { popUpTo("auth") { inclusive = true } }
                        }
                    }
                }
            } catch (e: ApiException) { e.printStackTrace() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(
            "settings",
            MODE_PRIVATE
        )

        val language = prefs.getString(
            "language",
            "es"
        ) ?: "es"

        android.util.Log.d("LANGUAGE_STARTUP", language)

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language)
        )
        super.onCreate(savedInstanceState)
        checkNotificationPermission()

        setContent {
            AlamuttRunningTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    navController = rememberNavController()
                    val trackingViewModel: TrackingViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        composable("splash") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                            LaunchedEffect(Unit) {
                                if (FirebaseAuth.getInstance().currentUser != null) {
                                    navController.navigate("quick_start") { popUpTo("splash") { inclusive = true } }
                                } else {
                                    navController.navigate("auth") { popUpTo("splash") { inclusive = true } }
                                }
                            }
                        }
                        composable("auth") {
                            val authViewModel: AuthViewModel = viewModel()
                            sharedAuthViewModel = authViewModel
                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    authViewModel.checkUserOnboardingStatus { shouldGoToOnboarding ->
                                        if (shouldGoToOnboarding) {
                                            navController.navigate("onboarding") { popUpTo("auth") { inclusive = true } }
                                        } else {
                                            navController.navigate("quick_start") { popUpTo("auth") { inclusive = true } }
                                        }
                                    }
                                },
                                onGoogleSignInClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) }
                            )
                        }
                        composable("onboarding") {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            OnboardingScreen(uid = uid, navController = navController)
                        }
                        composable("quick_start") {
                            QuickStartScreen(
                                navController = navController,
                                trackingViewModel = trackingViewModel,
                                onStartClick = { navController.navigate("countdown") },
                                onLogout = { googleSignInClient.signOut().addOnCompleteListener { navController.navigate("auth") { popUpTo("quick_start") { inclusive = true } } } },
                                onNavigateToHistory = { navController.navigate("run_history") }
                            )
                        }
                        composable("countdown") {
                            CountdownScreen(trackingViewModel, navController)
                        }
                        composable("tracking") {
                            TrackingScreen(
                                viewModel = trackingViewModel,
                                onFinish = { runId ->
                                    if (runId == "CANCELLED") {
                                        navController.navigate("quick_start") {
                                            popUpTo("tracking") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("run_detail/$runId") {
                                            popUpTo("tracking") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("run_history") {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            HistoryScreen(
                                viewModel = viewModel(),
                                userId = uid,
                                onBack = { navController.popBackStack() },
                                onRunClicked = { runId -> navController.navigate("run_detail/$runId") }
                            )
                        }
                        composable(
                            "run_detail/{runId}",
                            arguments = listOf(navArgument("runId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val runId = backStackEntry.arguments?.getString("runId") ?: ""
                            RunDetailScreen(
                                runId = runId,
                                viewModel = viewModel(),
                                onBack = {
                                    navController.navigate("quick_start") {
                                        popUpTo("quick_start") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("stats") { StatsScreen(navController) }
                        composable("profile") {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            ProfileScreen(uid = uid, navController = navController)
                        }
                        composable("leaderboard") { LeaderboardScreen(navController = navController) }
                        composable("challenges") {
                            ChallengesScreen(navController = navController)
                        }

                        composable(
                            "runner_profile/{uid}",
                            arguments = listOf(navArgument("uid") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val uid = backStackEntry.arguments?.getString("uid") ?: ""
                            RunnerProfileScreen(
                                uid = uid,
                                navController = navController
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                navController = navController
                            )
                        }

                        composable("language") {
                            LanguageScreen(
                                navController = navController
                            )
                        }
                        }
                    }
                }
            }
        }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}