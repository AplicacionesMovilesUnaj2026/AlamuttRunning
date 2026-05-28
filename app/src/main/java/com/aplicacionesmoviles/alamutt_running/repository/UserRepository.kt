package com.aplicacionesmoviles.alamutt_running.repository

import com.aplicacionesmoviles.alamutt_running.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveUser(user: User) {
        db.collection("users")
            .document(user.uid)
            .set(user)
    }

    fun updatePhoto(userId: String, photoUrl: String) {

        db.collection("users")
            .document(userId)
            .update("photoUrl", photoUrl)
    }

    suspend fun getPhoto(userId: String): String? {

        val document = db.collection("users")
            .document(userId)
            .get()
            .await()

        return document.getString("photoUrl")
    }
}