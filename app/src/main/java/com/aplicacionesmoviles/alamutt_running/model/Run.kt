package com.aplicacionesmoviles.alamutt_running.model

data class Run(
    val userId: String = "",
    val distance: Double = 0.0,
    val duration: Long = 0L,
    val calories: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)