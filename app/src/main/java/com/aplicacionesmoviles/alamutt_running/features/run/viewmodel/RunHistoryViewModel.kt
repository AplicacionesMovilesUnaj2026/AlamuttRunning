package com.aplicacionesmoviles.alamutt_running.features.run.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.core.domain.model.Run
import com.aplicacionesmoviles.alamutt_running.core.data.repository.RunRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RunRepository()
    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)
    
    val unitSystem = prefs.getString("unit_system", "Metric") ?: "Metric"
    private val _runHistory = MutableStateFlow<List<Run>>(emptyList())
    val runHistory: StateFlow<List<Run>> = _runHistory
    private var lastDocument: DocumentSnapshot? = null
    private var isLastPage = false

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadMoreRuns(userId: String) {
        val cleanId = userId.trim()
        if (_isLoading.value || isLastPage || cleanId.isEmpty()) return

        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val (newRuns, newLastDoc) = repository.getUserRunsPaginated(cleanId, lastDocument)

                if (newRuns.isEmpty()) {
                    isLastPage = true
                } else {
                    lastDocument = newLastDoc
                    _runHistory.value += newRuns
                    if (newRuns.size < 10) isLastPage = true
                }
            } catch (e: Exception) {
                // Error handling
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshHistory(userId: String) {
        lastDocument = null
        isLastPage = false
        _runHistory.value = emptyList()
        loadMoreRuns(userId)
    }
}
