package com.aplicacionesmoviles.alamutt_running.features.runnerProfile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.model.User
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import kotlinx.coroutines.launch

class RunnerProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository()
    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    val unitSystem = prefs.getString("unit_system", "Metric") ?: "Metric"

    var user by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun loadRunnerProfile(uid: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val data = userRepository.getUserData(uid)
                if (data != null) {
                    user = User(
                        uid = uid,
                        name = data["name"] as? String ?: "",
                        bio = data["bio"] as? String ?: "",
                        photoUrl = data["photoUrl"] as? String ?: "",
                        weightKg = (data["weightKg"] as? Number)?.toDouble() ?: 0.0,
                        heightCm = (data["heightCm"] as? Number)?.toInt() ?: 0,
                        totalDistance = (data["totalDistance"] as? Number)?.toDouble() ?: 0.0,
                        totalRuns = (data["totalRuns"] as? Number)?.toInt() ?: 0,
                        totalCalories = (data["totalCalories"] as? Number)?.toInt() ?: 0,
                        totalSteps = (data["totalSteps"] as? Number)?.toInt() ?: 0,
                        bestPace = (data["bestPace"] as? Number)?.toDouble() ?: 0.0
                    )
                } else {
                    error = "No se encontró el perfil del corredor"
                }
            } catch (e: Exception) {
                error = "Error al cargar el perfil: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
