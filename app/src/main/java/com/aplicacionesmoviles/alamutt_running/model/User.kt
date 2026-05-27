package com.aplicacionesmoviles.alamutt_running.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)