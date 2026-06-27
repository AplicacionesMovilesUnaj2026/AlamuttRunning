package com.aplicacionesmoviles.alamutt_running.features.run.ui

sealed interface RunState {
    object Idle : RunState
    object Countdown : RunState
    object Running : RunState
    object Paused : RunState
    // Error is for TERMINAL failures only (e.g. save failure).
    // GPS loss MUST NOT use this state — Error triggers resetTracking() which wipes the run.
    // Use TrackingViewModel.gpsLost: StateFlow<Boolean> for GPS-loss feedback instead.
    data class Error(val message: String) : RunState
}
