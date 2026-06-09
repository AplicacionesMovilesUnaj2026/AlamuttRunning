package com.aplicacionesmoviles.alamutt_running.repository

import com.aplicacionesmoviles.alamutt_running.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserData(userId: String): Map<String, Any>? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) document.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserStats(
        userId: String,
        distance: Double,
        calories: Int,
        steps: Int,
        pace: Double
    ): List<Double> {
        val userRef = db.collection("users").document(userId)
        val currentWeekId = getCurrentWeekId()
        val newlyCompleted = mutableListOf<Double>()

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentDistance = snapshot.getDouble("totalDistance") ?: 0.0
            val currentRuns = snapshot.getLong("totalRuns") ?: 0
            val currentCalories = snapshot.getLong("totalCalories")?.toInt() ?: 0
            val currentSteps = snapshot.getLong("totalSteps")?.toInt() ?: 0
            val bestPace = snapshot.getDouble("bestPace") ?: 0.0
            val currentPoints = snapshot.getLong("points")?.toInt() ?: 0

            val rawActiveChallenges = snapshot.get("activeChallenges") as? Map<String, Any>
            val challengeWeekId = snapshot.getString("challengeWeekId") ?: ""
            val completedChallenges = (snapshot.get("completedChallenges") as? List<*>)?.toMutableList() ?: mutableListOf()
            
            var newPoints = currentPoints
            val updatedActiveChallenges = mutableMapOf<String, Double>()
            
            if (challengeWeekId == currentWeekId && rawActiveChallenges != null) {
                rawActiveChallenges.forEach { (distKey, progressValue) ->
                    val targetDist = distKey.toDoubleOrNull() ?: 0.0
                    val currentProgress = (progressValue as? Number)?.toDouble() ?: 0.0
                    
                    if (targetDist > 0.0) {
                        val newProgress = currentProgress + distance
                        

                        if (currentProgress < targetDist && newProgress >= targetDist) {
                            newPoints += (targetDist * 10).toInt()
                            completedChallenges.add("${distKey}-$currentWeekId")
                            newlyCompleted.add(targetDist)

                        } else if (newProgress < targetDist) {
                            updatedActiveChallenges[distKey] = newProgress
                        }
                    }
                }
            } else if (challengeWeekId != currentWeekId) {
                // Nueva semana: los desafíos anteriores desaparecen (se reinician)
            }

            val newBestPace = if (bestPace == 0.0 || (pace > 0.0 && pace < bestPace)) pace else bestPace

            transaction.update(
                userRef,
                mapOf(
                    "totalDistance" to currentDistance + distance,
                    "totalRuns" to currentRuns + 1,
                    "totalCalories" to currentCalories + calories,
                    "totalSteps" to currentSteps + steps,
                    "bestPace" to newBestPace,
                    "activeChallenges" to updatedActiveChallenges,
                    "completedChallenges" to completedChallenges,
                    "challengeWeekId" to currentWeekId,
                    "points" to newPoints
                )
            )
        }.await()
        return newlyCompleted
    }

    suspend fun subscribeToChallenge(userId: String, distance: Double) {
        val weekId = getCurrentWeekId()
        val userRef = db.collection("users").document(userId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val challengeWeekId = snapshot.getString("challengeWeekId") ?: ""
            
            val activeChallenges = if (challengeWeekId == weekId) {
                (snapshot.get("activeChallenges") as? Map<*, *>)?.toMutableMap() ?: mutableMapOf()
            } else {
                mutableMapOf()
            }
            
            // Añadir el nuevo desafío si no existe
            val distKey = distance.toString()
            if (!activeChallenges.containsKey(distKey)) {
                activeChallenges[distKey] = 0.0
            }
            
            transaction.update(userRef, mapOf(
                "activeChallenges" to activeChallenges,
                "challengeWeekId" to weekId
            ))
        }.await()
    }

    suspend fun unsubscribeFromChallenge(userId: String, distance: Double) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val activeChallenges = (snapshot.get("activeChallenges") as? Map<String, Any>)?.toMutableMap() ?: mutableMapOf()
            activeChallenges.remove(distance.toString())
            transaction.update(userRef, "activeChallenges", activeChallenges)
        }.await()
    }

    private fun getCurrentWeekId(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        return "$year-W$week"
    }

    fun updateUserData(
        uid: String,
        name: String,
        bio: String,
        weightKg: Double,
        heightCm: Int,
        onResult: (Boolean) -> Unit
    ) {
        db.collection("users").document(uid)
            .update(
                mapOf(
                    "name" to name,
                    "bio" to bio,
                    "weightKg" to weightKg,
                    "heightCm" to heightCm
                )
            )
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun saveUser(user: User) {
        db.collection("users").document(user.uid).set(user)
    }

    fun updatePhoto(userId: String, photoUrl: String) {
        db.collection("users").document(userId).update("photoUrl", photoUrl)
    }
}
