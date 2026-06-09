package com.aplicacionesmoviles.alamutt_running.features.profile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val prefs = application.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)

    var unitSystem by mutableStateOf(prefs.getString("unit_system", "Metric") ?: "Metric")
        private set

    var name by mutableStateOf("")
    var bio by mutableStateOf("")
    var photoUrl by mutableStateOf<String?>(null)
    var weightKg by mutableStateOf(70.0)
    var heightCm by mutableStateOf(170)
    var points by mutableStateOf(0)

    var editName by mutableStateOf("")
    var editBio by mutableStateOf("")
    var editWeightKg by mutableStateOf("")
    var editHeightCm by mutableStateOf("")

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
                points = (it["points"] as? Number)?.toInt() ?: 0

                editName = name
                editBio = bio
                editWeightKg = weightKg.toString()
                editHeightCm = heightCm.toString()
            }
        }
    }

    fun saveChanges(uid: String, onComplete: () -> Unit) {
        val newWeight = editWeightKg.toDoubleOrNull() ?: 0.0
        val newHeight = editHeightCm.toIntOrNull() ?: 0

        if (newWeight <= 0.0 || newHeight <= 0) {
            // Podríamos añadir un estado de error aquí si quisiéramos mostrar un mensaje
            return
        }

        isSaving = true
        userRepository.updateUserData(uid, editName, editBio, newWeight, newHeight) { success ->
            isSaving = false
            if (success) {
                name = editName
                bio = editBio
                weightKg = newWeight
                heightCm = newHeight
                onComplete()
            }
        }
    }
}
