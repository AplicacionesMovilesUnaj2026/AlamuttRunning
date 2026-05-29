package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.services.TrackingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class TrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val metricsCalculator = RunMetricsCalculator(application)

    val runState = MutableStateFlow<RunState>(RunState.Idle)
    val timerSeconds = MutableStateFlow(0L)
    val distance = MutableStateFlow(0.0)
    val pace = MutableStateFlow(0.0)
    val calories = MutableStateFlow(0)
    val steps = MutableStateFlow(0)

    private val _routePoints = mutableListOf<Location>()
    val routePoints: List<Location> get() = _routePoints

    private var timerJob: Job? = null

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra("action")) {
                TrackingService.ACTION_PAUSE -> updateRunState(RunState.Paused)
                TrackingService.ACTION_RESUME -> updateRunState(RunState.Running)
            }
        }
    }

    init {
        context.registerReceiver(controlReceiver, IntentFilter("CONTROL_RUN"), Context.RECEIVER_EXPORTED)
    }

    fun startTracking() {
        val intent = Intent(context, TrackingService::class.java)
        androidx.core.content.ContextCompat.startForegroundService(context, intent)
        updateRunState(RunState.Running)
    }

    fun updateRunState(newState: RunState) {
        runState.value = newState
        when (newState) {
            is RunState.Running -> startTimer()
            is RunState.Paused -> timerJob?.cancel()
            else -> resetTracking()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (runState.value is RunState.Running) {
                delay(1000)
                timerSeconds.value += 1
                val timeString = String.format(Locale.US, "%02d:%02d", timerSeconds.value / 60, timerSeconds.value % 60)
                val intent = Intent(context, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_UPDATE
                    putExtra("time", timeString)
                }
                context.startService(intent)
            }
        }
    }

    fun processLocation(loc: Location) {
        if (runState.value is RunState.Running) {
            _routePoints.add(loc)

            val m = metricsCalculator.updateMetrics(loc)
            distance.value = m.distance
            pace.value = m.pace
            calories.value = m.calories
        }
    }

    fun updateSteps(s: Int) { steps.value = s }

    fun resetTracking() {
        timerJob?.cancel()
        metricsCalculator.reset()
        _routePoints.clear()
        timerSeconds.value = 0
        runState.value = RunState.Idle
        context.stopService(Intent(context, TrackingService::class.java))
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(controlReceiver)
        resetTracking()
    }
}