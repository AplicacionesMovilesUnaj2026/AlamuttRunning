package com.aplicacionesmoviles.alamutt_running.features.run.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.core.data.local.RunCheckpointStore
import com.aplicacionesmoviles.alamutt_running.core.data.repository.RunRepository
import com.aplicacionesmoviles.alamutt_running.core.domain.model.Run
import com.aplicacionesmoviles.alamutt_running.core.domain.model.RunCheckpoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Reads a persisted RunCheckpoint on init and exposes it for the recovery dialog.
 * No DI framework — direct instantiation consistent with existing VMs.
 */
class RunRecoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val checkpointStore = RunCheckpointStore(application)
    private val runRepository = RunRepository()

    private val _checkpoint = MutableStateFlow<RunCheckpoint?>(null)
    val checkpoint: StateFlow<RunCheckpoint?> = _checkpoint.asStateFlow()

    private val _saveError = MutableStateFlow(false)
    val saveError: StateFlow<Boolean> = _saveError.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = checkpointStore.read()
            // Discard checkpoints under 50 m — not worth recovering.
            if (saved != null && saved.distanceMeters >= 50.0) {
                _checkpoint.value = saved
            } else if (saved != null) {
                checkpointStore.clear()
            }
        }
    }

    /**
     * Saves the abandoned run to Firestore (offline queue applies if offline),
     * clears the checkpoint, and emits null so the dialog is dismissed.
     */
    fun saveRecoveredRun(onResult: (Boolean) -> Unit) {
        val c = _checkpoint.value ?: return
        viewModelScope.launch {
            try {
                val run = Run(
                    userId = c.userId,
                    distance = c.distanceMeters,
                    pace = c.pace,
                    duration = c.durationSeconds,
                    calories = c.calories,
                    steps = c.steps,
                    date = c.updatedAt
                )
                runRepository.saveRun(run)
                checkpointStore.clear()
                _checkpoint.value = null
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _saveError.value = true
                onResult(false)
            }
        }
    }

    fun clearSaveError() {
        _saveError.value = false
    }

    /**
     * Discards the abandoned run: clears the checkpoint and emits null.
     */
    fun discardRun() {
        viewModelScope.launch {
            checkpointStore.clear()
            _checkpoint.value = null
        }
    }
}
