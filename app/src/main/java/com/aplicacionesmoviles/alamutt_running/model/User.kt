package com.aplicacionesmoviles.alamutt_running.model

data class User(
    val uid: String = "",
    val email: String = "",
    val weightKg: Double = 70.0,
    val heightCm: Int = 170,
    val name: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val totalDistance: Double = 0.0,
    val totalRuns: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)