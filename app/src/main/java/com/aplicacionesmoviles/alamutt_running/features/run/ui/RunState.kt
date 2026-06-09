package com.aplicacionesmoviles.alamutt_running.features.run.ui

sealed interface RunState {
    object Idle : RunState
    object Countdown : RunState
    object Running : RunState
    object Paused : RunState
    data class Error(val message: String) : RunState
}
