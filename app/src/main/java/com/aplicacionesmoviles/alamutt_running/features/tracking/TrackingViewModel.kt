package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.model.User
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
    private var user: User = User()
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

    fun startTracking(userId: String) {

        loadUser(userId)

        val intent = Intent(context, TrackingService::class.java)
        ContextCompat.startForegroundService(context, intent)

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

            distance.value = m.distanceMeters
            pace.value = m.currentPace

            // Cálculo dinámico de calorías
            val met = if (m.currentPace > 0) {
                when {
                    m.currentPace < 6.0 -> 10.0 // Corriendo rápido
                    m.currentPace < 10.0 -> 7.0  // Trotando
                    else -> 3.5                // Caminando
                }
            } else {
                0.0
            }

            // Calorías por metro = (MET * 3.5 * peso) / (200 * velocidad_en_m/s * 60)
            val distanceKm = (m.distanceMeters / 1000.0)
            calories.value = (distanceKm * user.weightKg * 1.036).toInt()
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

    fun loadUser(userId: String) {
        viewModelScope.launch {

            val data = userRepository.getUserData(userId)

            data?.let {

                user = User(
                    uid = userId,
                    email = it["email"] as? String ?: "",
                    name = it["name"] as? String ?: "",
                    bio = it["bio"] as? String ?: "",
                    photoUrl = it["photoUrl"] as? String ?: "",
                    weightKg = (it["weightKg"] as? Number)?.toDouble() ?: 70.0,
                    heightCm = (it["heightCm"] as? Number)?.toInt() ?: 170,
                    createdAt = (it["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            }
        }
    }
}