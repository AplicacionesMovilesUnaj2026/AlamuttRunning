package com.aplicacionesmoviles.alamutt_running.features.run.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.core.domain.model.Run
import com.aplicacionesmoviles.alamutt_running.core.data.repository.RunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RunRepository()
    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    val unitSystem = prefs.getString("unit_system", "Metric") ?: "Metric"
    private val _run = MutableStateFlow<Run?>(null)
    val run: StateFlow<Run?> = _run

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRun(runId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val runData = repository.getRunById(runId)
                _run.value = runData
            } catch (e: Exception) {
                e.printStackTrace()
                _run.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
