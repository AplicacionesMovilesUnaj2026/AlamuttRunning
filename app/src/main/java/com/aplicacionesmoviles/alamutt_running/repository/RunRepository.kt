package com.aplicacionesmoviles.alamutt_running.repository

import com.aplicacionesmoviles.alamutt_running.model.Run
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RunRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveRun(run: Run) {

        db.collection("runs")
            .add(run)
    }

    suspend fun getTotalRuns(userId: String): Int {

        val result = db.collection("runs")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return result.size()
    }

    suspend fun getTotalDistance(userId: String): Double {

        val result = db.collection("runs")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return result.documents.sumOf {

            it.getDouble("distance") ?: 0.0
        }
    }
}