package com.aplicacionesmoviles.alamutt_running.features.RunHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.model.Run
import com.aplicacionesmoviles.alamutt_running.repository.RunRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunHistoryViewModel(private val repository: RunRepository) : ViewModel() {
    private val _runHistory = MutableStateFlow<List<Run>>(emptyList())
    val runHistory: StateFlow<List<Run>> = _runHistory
    private var lastDocument: DocumentSnapshot? = null
    private var isLastPage = false

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadMoreRuns(userId: String) {
        if (_isLoading.value || isLastPage || userId.isEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val (newRuns, newLastDoc) = repository.getUserRunsPaginated(userId, lastDocument)

                if (newRuns.isEmpty()) {
                    isLastPage = true
                } else {
                    lastDocument = newLastDoc
                    _runHistory.value = _runHistory.value + newRuns
                    if (newRuns.size < 10) isLastPage = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}