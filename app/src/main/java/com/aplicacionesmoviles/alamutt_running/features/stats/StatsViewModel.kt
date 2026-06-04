package com.aplicacionesmoviles.alamutt_running.features.stats

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.repository.RunRepository
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val runRepository = RunRepository()
    private val userRepository = UserRepository()
    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    val unitSystem = prefs.getString("unit_system", "Metric") ?: "Metric"

    var totalRuns by mutableIntStateOf(0)
        private set
    var totalDistanceKm by mutableDoubleStateOf(0.0)
        private set
    var totalCalories by mutableIntStateOf(0)
        private set
    var totalSteps by mutableIntStateOf(0)
        private set
    var isLoading by mutableStateOf(true)
        private set

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val userData = userRepository.getUserData(userId)
                if (userData != null) {
                    totalRuns = (userData["totalRuns"] as? Long)?.toInt() ?: 0
                    totalDistanceKm = (userData["totalDistance"] as? Number)?.toDouble() ?: 0.0
                    totalCalories = (userData["totalCalories"] as? Long)?.toInt() ?: 0
                    totalSteps = (userData["totalSteps"] as? Long)?.toInt() ?: 0
                } else {
                    // Fallback a calcular desde las carreras si no hay datos en el usuario
                    val runs = runRepository.getAllUserRuns(userId)
                    totalRuns = runs.size
                    totalDistanceKm = runs.sumOf { it.distance } / 1000.0
                    totalCalories = runs.sumOf { it.calories }
                    totalSteps = runs.sumOf { it.steps }
                }
            }
            isLoading = false
        }
    }
}
