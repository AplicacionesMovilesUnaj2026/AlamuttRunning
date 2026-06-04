package com.aplicacionesmoviles.alamutt_running.model

data class User(
    val uid: String = "",
    val email: String = "",
    val weightKg: Double = 0.0,
    val heightCm: Int = 0,
    val name: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val totalDistance: Double = 0.0,
    val totalRuns: Int = 0,
    val totalCalories: Int = 0,
    val totalSteps: Int = 0,
    val points: Int = 0,
    val activeChallenges: Map<String, Double> = emptyMap(),
    val challengeWeekId: String = "",
    val completedChallenges: List<String> = emptyList(),
    val bestPace: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)