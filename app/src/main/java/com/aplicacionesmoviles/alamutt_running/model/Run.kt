package com.aplicacionesmoviles.alamutt_running.model

data class Run(
    val id: String = "",
    val userId: String = "",
    val distance: Double = 0.0,
    val pace: Double = 0.0,
    val duration: Long = 0L,
    val calories: Int = 0,
    val steps: Int = 0,
    val date: Long = 0L,
)
