package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val metricsCalculator = RunMetricsCalculator(application)

    private val _runState = MutableStateFlow<RunState>(RunState.Idle)
    val runState: StateFlow<RunState> = _runState.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

    private val _distance = MutableStateFlow(0.0)
    val distance: StateFlow<Double> = _distance.asStateFlow()

    private val _pace = MutableStateFlow(0.0)
    val pace: StateFlow<Double> = _pace.asStateFlow()

    private val _calories = MutableStateFlow(0)
    val calories: StateFlow<Int> = _calories.asStateFlow()

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private var timerJob: Job? = null

    fun updateRunState(newState: RunState) {
        _runState.value = newState
        if (newState is RunState.Running) startTimer() else timerJob?.cancel()
    }

    fun processLocation(newLocation: Location) {
        if (_runState.value is RunState.Running) {
            val metrics = metricsCalculator.updateMetrics(newLocation)
            _distance.value = metrics.distance
            _pace.value = metrics.pace
            _calories.value = metrics.calories
        }
    }

    fun updateSteps(newSteps: Int) {
        _steps.value = newSteps
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_runState.value is RunState.Running) {
                delay(1000)
                _timerSeconds.value += 1
            }
        }
    }

    fun resetTracking() {
        timerJob?.cancel()
        metricsCalculator.reset()
        _timerSeconds.value = 0L
        _distance.value = 0.0
        _pace.value = 0.0
        _calories.value = 0
        _steps.value = 0
        _runState.value = RunState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        metricsCalculator.reset()
    }
}