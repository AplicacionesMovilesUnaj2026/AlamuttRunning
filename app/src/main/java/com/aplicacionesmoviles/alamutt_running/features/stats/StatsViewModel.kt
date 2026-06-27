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
import com.aplicacionesmoviles.alamutt_running.core.data.repository.RunRepository
import com.aplicacionesmoviles.alamutt_running.core.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    var totalPoints by mutableIntStateOf(0)
        private set

    var rankDistance by mutableIntStateOf(0)
        private set
    var rankCalories by mutableIntStateOf(0)
        private set
    var rankSteps by mutableIntStateOf(0)
        private set
    var rankPoints by mutableIntStateOf(0)
        private set
    var rankRuns by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val userData = userRepository.getUserData(userId)
                    if (userData != null) {
                        totalRuns = (userData["totalRuns"] as? Number)?.toInt() ?: 0
                        totalDistanceKm = (userData["totalDistance"] as? Number)?.toDouble() ?: 0.0
                        totalCalories = (userData["totalCalories"] as? Number)?.toInt() ?: 0
                        totalSteps = (userData["totalSteps"] as? Number)?.toInt() ?: 0
                        totalPoints = (userData["points"] as? Number)?.toInt() ?: 0

                        val db = FirebaseFirestore.getInstance()
                        val usersColl = db.collection("users")

                        fun safeRank(field: String, value: Any) = async {
                            try { usersColl.whereGreaterThan(field, value).count().get(AggregateSource.SERVER).await().count + 1 }
                            catch (_: Exception) { 0L }
                        }

                        val rDist   = safeRank("totalDistance", totalDistanceKm)
                        val rCal    = safeRank("totalCalories", totalCalories)
                        val rSteps  = safeRank("totalSteps", totalSteps)
                        val rPoints = safeRank("points", totalPoints)
                        val rRuns   = safeRank("totalRuns", totalRuns)

                        rankDistance = rDist.await().toInt()
                        rankCalories = rCal.await().toInt()
                        rankSteps    = rSteps.await().toInt()
                        rankPoints   = rPoints.await().toInt()
                        rankRuns     = rRuns.await().toInt()
                    } else {
                        val runs = runRepository.getAllUserRuns(userId)
                        totalRuns = runs.size
                        totalDistanceKm = runs.sumOf { it.distance } / 1000.0
                        totalCalories = runs.sumOf { it.calories }
                        totalSteps = runs.sumOf { it.steps }
                        totalPoints = runs.sumOf { it.points }
                    }
                }
            } catch (_: Exception) {
                // Load failed silently — screen shows zeroes
            } finally {
                isLoading = false
            }
        }
    }
}
