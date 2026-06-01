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
        distance: Double
    ) {

        val userRef =
            db.collection("users")
                .document(userId)

        db.runTransaction { transaction ->

            val snapshot =
                transaction.get(userRef)

            val currentDistance =
                snapshot.getDouble("totalDistance") ?: 0.0

            val currentRuns =
                snapshot.getLong("totalRuns") ?: 0

            transaction.update(
                userRef,
                mapOf(
                    "totalDistance" to currentDistance + distance,
                    "totalRuns" to currentRuns + 1
                )
            )
        }.await()
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