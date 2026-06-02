package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.model.Run
import com.aplicacionesmoviles.alamutt_running.model.User
import com.aplicacionesmoviles.alamutt_running.repository.RunRepository
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.services.TrackingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class TrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val userRepository = UserRepository()
    private val runRepository = RunRepository()
    private var user: User = User()
    private val metricsCalculator = RunMetricsCalculator(application)

    val runState = MutableStateFlow<RunState>(RunState.Idle)
    val timerSeconds = MutableStateFlow(0L)
    val distance = MutableStateFlow(0.0)
    val pace = MutableStateFlow(0.0)
    val calories = MutableStateFlow(0)
    val steps = MutableStateFlow(0)

    private val _routePoints = mutableListOf<Location>()
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

    private fun updateServiceNotification(time: String) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_UPDATE
            putExtra("time", time)
        }
        context.startService(intent)
    }

    fun finishAndSaveRun(userId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            if (distance.value < 50.0) {
                resetTracking()
                onResult(null)
                return@launch
            }

            val run = Run(
                userId = userId,
                distance = distance.value,
                pace = pace.value,
                duration = timerSeconds.value,
                calories = calories.value,
                steps = steps.value,
                date = System.currentTimeMillis()
            )

            val runId: String = runRepository.saveRun(run)
            userRepository.updateUserStats(userId, distance.value / 1000.0)
            resetTracking()
            onResult(runId)
        }
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
        context.startService(Intent(context, TrackingService::class.java))

        timerJob = viewModelScope.launch {
            while (runState.value is RunState.Running) {
                delay(1000)
                timerSeconds.value += 1
                val minutes = timerSeconds.value / 60
                val seconds = timerSeconds.value % 60
                updateServiceNotification(String.format(Locale.US, "%02d:%02d", minutes, seconds))
            }
        }
    }

    fun processLocation(loc: Location) {
        if (runState.value is RunState.Running) {
            if (loc.hasSpeed() && loc.speed > 6.0f) return
            _routePoints.add(loc)
            val m = metricsCalculator.updateMetrics(loc)
            distance.value = m.distanceMeters
            pace.value = m.currentPace
            calories.value = ((m.distanceMeters / 1000.0) * user.weightKg * 1.036).toInt()
        }
    }

    fun updateSteps(s: Int) { steps.value = s }

    fun resetTracking() {
        timerJob?.cancel()
        metricsCalculator.reset()
        _routePoints.clear()
        timerSeconds.value = 0
        distance.value = 0.0
        pace.value = 0.0
        calories.value = 0
        steps.value = 0
        runState.value = RunState.Idle
        context.stopService(Intent(context, TrackingService::class.java))
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(controlReceiver)
        resetTracking()
    }
}