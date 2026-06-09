package com.aplicacionesmoviles.alamutt_running.features.run.domain

data class RunMetrics(
    val distanceMeters: Double,
    val currentPace: Double,
    val averagePace: Double,
    val movingTimeMillis: Long
)
