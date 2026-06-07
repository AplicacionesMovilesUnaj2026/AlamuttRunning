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
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val userData = userRepository.getUserData(userId)
                if (userData != null) {
                    totalRuns = (userData["totalRuns"] as? Long)?.toInt() ?: 0
                    totalDistanceKm = (userData["totalDistance"] as? Number)?.toDouble() ?: 0.0
                    totalCalories = (userData["totalCalories"] as? Long)?.toInt() ?: 0
                    totalSteps = (userData["totalSteps"] as? Long)?.toInt() ?: 0
                    totalPoints = (userData["points"] as? Long)?.toInt() ?: 0

                    // Fetch ranks in parallel
                    val db = FirebaseFirestore.getInstance()
                    val usersColl = db.collection("users")

                    val rDist = async { usersColl.whereGreaterThan("totalDistance", totalDistanceKm).count().get(AggregateSource.SERVER).await().count + 1 }
                    val rCal = async { usersColl.whereGreaterThan("totalCalories", totalCalories).count().get(AggregateSource.SERVER).await().count + 1 }
                    val rSteps = async { usersColl.whereGreaterThan("totalSteps", totalSteps).count().get(AggregateSource.SERVER).await().count + 1 }
                    val rPoints = async { usersColl.whereGreaterThan("points", totalPoints).count().get(AggregateSource.SERVER).await().count + 1 }
                    val rRuns = async { usersColl.whereGreaterThan("totalRuns", totalRuns).count().get(AggregateSource.SERVER).await().count + 1 }

                    rankDistance = rDist.await().toInt()
                    rankCalories = rCal.await().toInt()
                    rankSteps = rSteps.await().toInt()
                    rankPoints = rPoints.await().toInt()
                    rankRuns = rRuns.await().toInt()
                } else {
                    val runs = runRepository.getAllUserRuns(userId)
                    totalRuns = runs.size
                    totalDistanceKm = runs.sumOf { it.distance } / 1000.0
                    totalCalories = runs.sumOf { it.calories }
                    totalSteps = runs.sumOf { it.steps }
                    totalPoints = runs.sumOf { it.points }
                }
            }
            isLoading = false
        }
    }
}
