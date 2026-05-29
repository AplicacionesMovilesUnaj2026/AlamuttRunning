package com.aplicacionesmoviles.alamutt_running.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    var name by mutableStateOf("")
    var bio by mutableStateOf("")
    var photoUrl by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)

    fun loadUserData(uid: String) {
        isLoading = true
        viewModelScope.launch {
            val userData = userRepository.getUserData(uid)
            userData?.let { data ->
                name = data["name"] as? String ?: ""
                bio = data["bio"] as? String ?: ""
                photoUrl = data["photoUrl"] as? String
            }
            isLoading = false
        }
    }

    fun updateProfile(uid: String, newName: String, newBio: String, onComplete: () -> Unit = {}) {
        isSaving = true
        userRepository.updateUserData(uid, newName, newBio) { success ->
            isSaving = false
            if (success) onComplete()
        }
    }
}