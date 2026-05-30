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
    var weightKg by mutableStateOf(70.0)
    var heightCm by mutableStateOf(170)

    var editName by mutableStateOf("")
    var editBio by mutableStateOf("")
    var editWeightKg by mutableStateOf(70.0)
    var editHeightCm by mutableStateOf(170)

    var isSaving by mutableStateOf(false)

    fun loadUserData(uid: String) {
        viewModelScope.launch {
            val data = userRepository.getUserData(uid)
            data?.let {
                name = it["name"] as? String ?: ""
                bio = it["bio"] as? String ?: ""
                photoUrl = it["photoUrl"] as? String
                weightKg = (it["weightKg"] as? Number)?.toDouble() ?: 70.0
                heightCm = (it["heightCm"] as? Number)?.toInt() ?: 170

                editName = name
                editBio = bio
                editWeightKg = weightKg
                editHeightCm = heightCm
            }
        }
    }

    fun saveChanges(uid: String, onComplete: () -> Unit) {
        isSaving = true
        userRepository.updateUserData(uid, editName, editBio, editWeightKg, editHeightCm) { success ->
            isSaving = false
            if (success) {
                name = editName
                bio = editBio
                weightKg = editWeightKg
                heightCm = editHeightCm
                onComplete()
            }
        }
    }
}