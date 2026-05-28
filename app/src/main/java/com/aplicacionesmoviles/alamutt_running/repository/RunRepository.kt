package com.aplicacionesmoviles.alamutt_running.repository

import com.aplicacionesmoviles.alamutt_running.model.Run
import com.google.firebase.firestore.FirebaseFirestore

class RunRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveRun(run: Run) {

        db.collection("runs")
            .add(run)
    }
}