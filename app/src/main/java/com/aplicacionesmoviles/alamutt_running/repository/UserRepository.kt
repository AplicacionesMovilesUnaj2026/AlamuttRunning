package com.aplicacionesmoviles.alamutt_running.repository

import com.aplicacionesmoviles.alamutt_running.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveUser(user: User) {
        db.collection("users")
            .document(user.uid)
            .set(user)
    }
}