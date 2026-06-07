package com.aplicacionesmoviles.alamutt_running.features.run.viewmodel

import com.aplicacionesmoviles.alamutt_running.features.run.ui.RunState
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.core.domain.model.Run
import com.aplicacionesmoviles.alamutt_running.core.domain.model.User
import com.aplicacionesmoviles.alamutt_running.core.data.repository.RunRepository
import com.aplicacionesmoviles.alamutt_running.core.data.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.features.run.data.TrackingService
import com.aplicacionesmoviles.alamutt_running.features.run.domain.RunMetricsCalculator
import com.google.firebase.auth.FirebaseAuth
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class TrackingViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val context = application
    private val userRepository = UserRepository()
    private val runRepository = RunRepository()
    private var user: User = User(weightKg = 70.0)
    private val metricsCalculator = RunMetricsCalculator(application)
    
    private var tts: TextToSpeech? = null
    private var lastSpokenKm = 0

    val runState = MutableStateFlow<RunState>(RunState.Idle)
    val timerSeconds = MutableStateFlow(0L)
    val distance = MutableStateFlow(0.0)
    val pace = MutableStateFlow(0.0)
    val calories = MutableStateFlow(0)
    val steps = MutableStateFlow(0)

    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    val goalDistance = MutableStateFlow(prefs.getFloat("goal_distance", 0.0f).toDouble())
    val isGoalReached = MutableStateFlow(false)

    val unitSystem = MutableStateFlow(prefs.getString("unit_system", "Metric") ?: "Metric")
    val countdownTime = MutableStateFlow(prefs.getInt("countdown_time", 3))
    val voiceAlertsEnabled = MutableStateFlow(prefs.getBoolean("voice_alerts", true))
    val voiceAlertFrequency = MutableStateFlow(prefs.getFloat("voice_alert_frequency", 1.0f).toDouble())

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
        tts = TextToSpeech(application, this)
        loadUserData()

        viewModelScope.launch {
            unitSystem.collect { prefs.edit().putString("unit_system", it).apply() }
        }
        viewModelScope.launch {
            countdownTime.collect { prefs.edit().putInt("countdown_time", it).apply() }
        }
        viewModelScope.launch {
            voiceAlertsEnabled.collect { prefs.edit().putBoolean("voice_alerts", it).apply() }
        }
        viewModelScope.launch {
            voiceAlertFrequency.collect { prefs.edit().putFloat("voice_alert_frequency", it.toFloat()).apply() }
        }
        viewModelScope.launch {
            goalDistance.collect { prefs.edit().putFloat("goal_distance", it.toFloat()).apply() }
        }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val data = userRepository.getUserData(userId)
                data?.let {
                    val weight = (it["weightKg"] as? Number)?.toDouble() ?: 70.0
                    user = User(
                        uid = userId,
                        weightKg = if (weight > 0.0) weight else 70.0,
                        name = it["name"] as? String ?: ""
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "ES")
        }
    }

    private fun speak(text: String) {
        if (voiceAlertsEnabled.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun updateServiceNotification(time: String) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_UPDATE
            putExtra("time", time)
        }
        context.startService(intent)
    }

    val completedChallenges = MutableStateFlow<List<Double>>(emptyList())

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
            val newlyCompleted = userRepository.updateUserStats(
                userId = userId,
                distance = distance.value / 1000.0,
                calories = calories.value,
                steps = steps.value,
                pace = pace.value
            )
            completedChallenges.value = newlyCompleted
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
            
            // Caloria : distancia(km) * peso(kg) * coeficiente de eficiencia 1.036
            calories.value = ((m.distanceMeters / 1000.0) * user.weightKg * 1.036).toInt()

            if (goalDistance.value > 0.0 && !isGoalReached.value) {
                if (m.distanceMeters >= goalDistance.value) {
                    isGoalReached.value = true
                    speak("¡Objetivo alcanzado! Excelente trabajo.")
                }
            }

            val isMetric = unitSystem.value == "Metric"
            val distanceInUnits = if (isMetric) (m.distanceMeters / 1000.0) else (m.distanceMeters * 0.000621371)
            val currentUnitCount = (distanceInUnits / voiceAlertFrequency.value).toInt()

            if (currentUnitCount > lastSpokenKm) {
                lastSpokenKm = currentUnitCount
                val totalDistanceSpoken = currentUnitCount * voiceAlertFrequency.value
                val unitLabel = UnitConverter.getFullUnitLabel(unitSystem.value)
                val paceLabel = UnitConverter.getPaceFullLabel(unitSystem.value)

                val displayPace = if (isMetric) m.currentPace else m.currentPace / 0.621371
                
                val distanceFormatted = if (totalDistanceSpoken % 1.0 == 0.0) {
                    totalDistanceSpoken.toInt().toString()
                } else {
                    String.format(Locale.US, "%.1f", totalDistanceSpoken)
                }
                
                speak("Distancia: $distanceFormatted $unitLabel. Ritmo: ${formatPace(displayPace)} $paceLabel.")
            }
        }
    }

    private fun formatPace(pace: Double): String {
        if (pace <= 0.0 || pace.isNaN() || pace.isInfinite()) return "cero"
        val minutes = pace.toInt()
        val seconds = ((pace - minutes) * 60).toInt()
        return if (minutes > 0) {
            "$minutes minutos y $seconds segundos"
        } else {
            "$seconds segundos"
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
        isGoalReached.value = false
        lastSpokenKm = 0
        runState.value = RunState.Idle
        context.stopService(Intent(context, TrackingService::class.java))
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(controlReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tts?.stop()
        tts?.shutdown()
        resetTracking()
    }
}
