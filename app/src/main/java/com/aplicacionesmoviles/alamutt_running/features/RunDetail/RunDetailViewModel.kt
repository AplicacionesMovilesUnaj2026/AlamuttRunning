package com.aplicacionesmoviles.alamutt_running.features.RunDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.model.Run
import com.aplicacionesmoviles.alamutt_running.repository.RunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunDetailViewModel(private val repository: RunRepository) : ViewModel() {
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