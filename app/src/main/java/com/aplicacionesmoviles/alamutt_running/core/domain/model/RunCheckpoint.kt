package com.aplicacionesmoviles.alamutt_running.core.domain.model

/**
 * Snapshot of an in-progress run, persisted to DataStore every ~30 s.
 * Cleared on successful run save; survives process kill for abandoned-run recovery.
 */
data class RunCheckpoint(
    val userId: String,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val pace: Double,
    val calories: Int,
    val steps: Int,
    val updatedAt: Long
)
