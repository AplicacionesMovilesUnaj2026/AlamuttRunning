package com.aplicacionesmoviles.alamutt_running.repository

import com.aplicacionesmoviles.alamutt_running.model.Run
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class RunRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveRun(run: Run): String {
        val documentRef = db.collection("runs").add(run).await()
        return documentRef.id
    }

    suspend fun getAllUserRuns(userId: String): List<Run> {
        val result = db.collection("runs")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return result.documents.map { doc ->
            doc.toObject(Run::class.java)?.copy(id = doc.id) ?: Run()
        }
    }

    suspend fun getUserRunsPaginated(userId: String, lastDocument: DocumentSnapshot? = null): Pair<List<Run>, DocumentSnapshot?> {
        var query = db.collection("runs")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(10)

        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        val result = query.get().await()
        val runs = result.documents.map { doc ->
            doc.toObject(Run::class.java)?.copy(id = doc.id) ?: Run()
        }

        val newLastDocument = if (result.documents.isNotEmpty()) result.documents.last() else null

        return Pair(runs, newLastDocument)
    }

    suspend fun getRunById(runId: String): Run? {
        val document = db.collection("runs")
            .document(runId)
            .get()
            .await()
        return document.toObject(Run::class.java)?.copy(id = document.id)
    }
}