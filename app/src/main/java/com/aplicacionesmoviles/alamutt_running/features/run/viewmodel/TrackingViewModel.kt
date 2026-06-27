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
import com.aplicacionesmoviles.alamutt_running.core.data.local.RunCheckpointStore
import com.aplicacionesmoviles.alamutt_running.core.domain.model.Run
import com.aplicacionesmoviles.alamutt_running.core.domain.model.RunCheckpoint
import com.aplicacionesmoviles.alamutt_running.core.domain.model.User
import com.aplicacionesmoviles.alamutt_running.core.data.repository.RunRepository
import com.aplicacionesmoviles.alamutt_running.core.data.repository.UserRepository
import com.aplicacionesmoviles.alamutt_running.features.run.data.TrackingNotificationService
import com.aplicacionesmoviles.alamutt_running.features.run.domain.RunMetricsCalculator
import com.google.firebase.auth.FirebaseAuth
import com.aplicacionesmoviles.alamutt_running.core.common.util.UnitConverter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class TrackingViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    companion object {
        private const val DEFAULT_STRIDE_METERS = 0.75
        // Accuracy threshold above which we treat the fix as GPS-unavailable and
        // fall back to dead-reckoning. FusedLocationProvider can report
        // isLocationAvailable=true via WiFi/cell even when GPS satellites are absent;
        // checking accuracy here catches that gap.
        private const val POOR_GPS_THRESHOLD_M = 25f
    }

    private var strideMeters = DEFAULT_STRIDE_METERS
    private var calibrationGpsMeters = 0.0
    private var calibrationStepCount = 0

    private val context = application
    private val userRepository = UserRepository()
    private val runRepository = RunRepository()
    private var user: User = User(weightKg = 70.0)
    private val metricsCalculator = RunMetricsCalculator(application)

    // Injected via direct instantiation — consistent with the no-DI-framework pattern.
    private val checkpointStore: RunCheckpointStore = RunCheckpointStore(application)

    private var tts: TextToSpeech? = null
    private var lastSpokenKm = 0

    val runState = MutableStateFlow<RunState>(RunState.Idle)
    val timerSeconds = MutableStateFlow(0L)
    val distance = MutableStateFlow(0.0)
    val pace = MutableStateFlow(0.0)
    val calories = MutableStateFlow(0)
    val steps = MutableStateFlow(0)

    // GPS loss: use this parallel flag, NOT RunState.Error.
    // CRITICAL: RunState.Error triggers resetTracking() via updateRunState()'s else branch,
    // which would wipe the active run. A separate boolean avoids touching the run state machine.
    private val _gpsLost = MutableStateFlow(false)
    val gpsLost: StateFlow<Boolean> = _gpsLost.asStateFlow()

    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    val goalDistance = MutableStateFlow(prefs.getFloat("goal_distance", 0.0f).toDouble())
    val isGoalReached = MutableStateFlow(false)

    val unitSystem = MutableStateFlow(prefs.getString("unit_system", "Metric") ?: "Metric")
    val countdownTime = MutableStateFlow(prefs.getInt("countdown_time", 3))
    val voiceAlertsEnabled = MutableStateFlow(prefs.getBoolean("voice_alerts", true))
    val voiceAlertFrequency = MutableStateFlow(prefs.getFloat("voice_alert_frequency", 1.0f).toDouble())

    private var lastStepCount = 0

    private val _routePoints = mutableListOf<Location>()
    private var timerJob: Job? = null
    private var checkpointJob: Job? = null

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                TrackingNotificationService.ACTION_LOCATION_UPDATE -> {
                    if (intent.hasExtra(TrackingNotificationService.EXTRA_GPS_AVAILABLE)) {
                        val available = intent.getBooleanExtra(TrackingNotificationService.EXTRA_GPS_AVAILABLE, true)
                        onGpsAvailabilityChanged(available)
                    } else {
                        val loc = android.location.Location("fused").apply {
                            latitude = intent.getDoubleExtra(TrackingNotificationService.EXTRA_LAT, 0.0)
                            longitude = intent.getDoubleExtra(TrackingNotificationService.EXTRA_LNG, 0.0)
                            accuracy = intent.getFloatExtra(TrackingNotificationService.EXTRA_ACCURACY, 0f)
                            speed = intent.getFloatExtra(TrackingNotificationService.EXTRA_SPEED, 0f)
                            bearing = intent.getFloatExtra(TrackingNotificationService.EXTRA_BEARING, 0f)
                            time = intent.getLongExtra(TrackingNotificationService.EXTRA_TIME, 0L)
                        }
                        processLocation(loc)
                    }
                }
                "CONTROL_RUN" -> {
                    when (intent.getStringExtra("action")) {
                        TrackingNotificationService.ACTION_PAUSE -> updateRunState(RunState.Paused)
                        TrackingNotificationService.ACTION_RESUME -> updateRunState(RunState.Running)
                    }
                }
            }
        }
    }

    init {
        context.registerReceiver(
            controlReceiver,
            IntentFilter("CONTROL_RUN").apply { addAction(TrackingNotificationService.ACTION_LOCATION_UPDATE) },
            Context.RECEIVER_EXPORTED
        )
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
        val paceStr = formatPaceCompact(pace.value)
        val distStr = String.format(Locale.US, "%.2f km", distance.value / 1000.0)
        val intent = Intent(context, TrackingNotificationService::class.java).apply {
            action = TrackingNotificationService.ACTION_UPDATE
            putExtra("time", time)
            putExtra("distance", distStr)
            putExtra("pace", paceStr)
        }
        context.startService(intent)
    }

    private fun formatPaceCompact(pace: Double): String {
        if (pace <= 0.0 || pace.isNaN() || pace.isInfinite()) return "--:--"
        val minutes = pace.toInt()
        val seconds = ((pace - minutes) * 60).toInt()
        return String.format(Locale.US, "%d:%02d /km", minutes, seconds)
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

            try {
                val runId: String = runRepository.saveRun(run)
                // Save committed or queued in Firestore offline cache — safe to clear.
                checkpointStore.clear()
                resetTracking()
                // Stats update is best-effort — it requires network (transaction).
                // Failure here does not affect the run save; stats will be inconsistent
                // until the next online session. Never block the user flow for this.
                try {
                    val newlyCompleted = userRepository.updateUserStats(
                        userId = userId,
                        distance = distance.value / 1000.0,
                        calories = calories.value,
                        steps = steps.value,
                        pace = pace.value
                    )
                    completedChallenges.value = newlyCompleted
                } catch (statsError: Exception) {
                    statsError.printStackTrace()
                }
                onResult(runId)
            } catch (e: Exception) {
                e.printStackTrace()
                checkpointStore.clear()
                resetTracking()
                onResult(null)
            }
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
        context.startService(Intent(context, TrackingNotificationService::class.java))

        timerJob = viewModelScope.launch {
            while (runState.value is RunState.Running) {
                delay(1000)
                timerSeconds.value += 1
                val minutes = timerSeconds.value / 60
                val seconds = timerSeconds.value % 60
                updateServiceNotification(String.format(Locale.US, "%02d:%02d", minutes, seconds))
            }
        }

        startCheckpointing()
    }

    /**
     * Launches a sibling coroutine that writes a checkpoint every 30 seconds
     * while the run is in Running state. Cancelled by resetTracking().
     * NFR-RP-1: DataStore writes are async; no main-thread blocking occurs.
     */
    private fun startCheckpointing() {
        checkpointJob?.cancel()
        checkpointJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                if (runState.value is RunState.Running) {
                    writeCheckpoint()
                }
            }
        }
    }

    private suspend fun writeCheckpoint() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        checkpointStore.save(
            RunCheckpoint(
                userId = userId,
                distanceMeters = distance.value,
                durationSeconds = timerSeconds.value,
                pace = pace.value,
                calories = calories.value,
                steps = steps.value,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Called by the UI layer when the FusedLocationProviderClient reports a change
     * in location availability. GPS loss does NOT stop the timer or use RunState.Error —
     * it only freezes distance accumulation and shows a UI banner.
     */
    fun onGpsAvailabilityChanged(available: Boolean) {
        if (available && _gpsLost.value) metricsCalculator.resetLastLocation()
        if (!available) {
            calibrationGpsMeters = 0.0
            calibrationStepCount = 0
        }
        _gpsLost.value = !available
    }

    fun processLocation(loc: Location) {
        if (runState.value is RunState.Running) {
            // Poor-accuracy fixes (WiFi/cell) count as GPS lost — isLocationAvailable
            // stays true via network providers even when satellites are unavailable.
            if (loc.accuracy > POOR_GPS_THRESHOLD_M) {
                if (!_gpsLost.value) onGpsAvailabilityChanged(false)
                return
            }
            // Good accuracy after a GPS-lost period — recover before processing.
            if (_gpsLost.value) onGpsAvailabilityChanged(true)
            if (loc.hasSpeed() && loc.speed > 6.0f) return
            _routePoints.add(loc)
            val m = metricsCalculator.updateMetrics(loc)
            distance.value = m.distanceMeters
            pace.value = if (m.averagePace > 0.0) m.averagePace else m.currentPace
            
            calories.value = computeCalories(m.distanceMeters)
            calibrationGpsMeters += metricsCalculator.lastValidGpsDistanceDelta
            tryCalibrate()

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

                val displayPace = if (isMetric) m.averagePace else m.averagePace / 0.621371
                
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

    private fun computeCalories(distanceMeters: Double): Int =
        ((distanceMeters / 1000.0) * user.weightKg * 1.036).toInt()

    private fun tryCalibrate() {
        if (calibrationGpsMeters >= 100.0 && calibrationStepCount > 0) {
            val rawStride = (calibrationGpsMeters / calibrationStepCount).coerceIn(0.50, 1.20)
            strideMeters = 0.7 * strideMeters + 0.3 * rawStride
            calibrationGpsMeters = 0.0
            calibrationStepCount = 0
        }
    }

    fun updateSteps(s: Int) {
        val delta = (s - lastStepCount).coerceAtLeast(0)
        lastStepCount = s
        steps.value = s
        if (runState.value is RunState.Running) {
            calibrationStepCount += delta
        }
        if (_gpsLost.value && runState.value is RunState.Running) {
            val newDistance = metricsCalculator.addDeadReckoningSteps(delta, strideMeters)
            distance.value = newDistance
            calories.value = computeCalories(newDistance)
            val elapsedMin = timerSeconds.value / 60.0
            if (newDistance > 0 && elapsedMin > 0) {
                pace.value = elapsedMin / (newDistance / 1000.0)
            }
        }
    }

    fun resetTracking() {
        timerJob?.cancel()
        checkpointJob?.cancel()
        metricsCalculator.reset()
        _routePoints.clear()
        timerSeconds.value = 0
        distance.value = 0.0
        pace.value = 0.0
        calories.value = 0
        steps.value = 0
        lastStepCount = 0
        strideMeters = DEFAULT_STRIDE_METERS
        calibrationGpsMeters = 0.0
        calibrationStepCount = 0
        isGoalReached.value = false
        lastSpokenKm = 0
        _gpsLost.value = false
        runState.value = RunState.Idle
        context.stopService(Intent(context, TrackingNotificationService::class.java))
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
