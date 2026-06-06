package com.aplicacionesmoviles.alamutt_running.features.leaderboard

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    val unitSystem = prefs.getString("unit_system", "Metric") ?: "Metric"

    var selectedFilter by mutableStateOf(LeaderboardFilter.DISTANCE)
    var users by mutableStateOf<List<LeaderboardUser>>(emptyList())
    var isLoading by mutableStateOf(true)
        private set

    fun updateFilter(filter: LeaderboardFilter) {
        selectedFilter = filter
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            isLoading = true
            val direction = if (selectedFilter == LeaderboardFilter.PACE) Query.Direction.ASCENDING else Query.Direction.DESCENDING
            
            val query = FirebaseFirestore.getInstance()
                .collection("users")
                .whereGreaterThan(selectedFilter.field, if (selectedFilter == LeaderboardFilter.PACE) 0.0 else 0)
                .orderBy(selectedFilter.field, direction)
                .limit(50)

            try {
                val result = query.get().await()
                users = result.documents.map {
                    LeaderboardUser(
                        uid = it.id,
                        name = it.getString("name") ?: "",
                        photoUrl = it.getString("photoUrl") ?: "",
                        totalDistance = it.getDouble("totalDistance") ?: 0.0,
                        totalCalories = it.getLong("totalCalories")?.toInt() ?: 0,
                        totalSteps = it.getLong("totalSteps")?.toInt() ?: 0,
                        points = it.getLong("points")?.toInt() ?: 0,
                        bestPace = it.getDouble("bestPace") ?: 0.0
                    )
                }
            } catch (e: Exception) {
                users = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
}
