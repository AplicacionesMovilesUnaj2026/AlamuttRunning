package com.aplicacionesmoviles.alamutt_running.features.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.aplicacionesmoviles.alamutt_running.model.User
import com.aplicacionesmoviles.alamutt_running.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    fun loginWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            uiState = AuthUiState.Error("Complete todos los campos.")
            return
        }
        uiState = AuthUiState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                uiState = AuthUiState.Success
                onSuccess()
            }
            .addOnFailureListener { e ->
                uiState = AuthUiState.Error(mapFirebaseError(e))
            }
    }

    fun registerWithEmail(email: String, pass: String, confirm: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank() || confirm.isBlank()) {
            uiState = AuthUiState.Error("Complete todos los campos.")
            return
        }
        if (pass != confirm) {
            uiState = AuthUiState.Error("Las contraseñas no coinciden.")
            return
        }
        uiState = AuthUiState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val user = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: generateRandomName(),
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    userRepository.saveUser(user)
                }
                uiState = AuthUiState.Success
                onSuccess()
            }
            .addOnFailureListener { e ->
                uiState = AuthUiState.Error(mapFirebaseError(e))
            }
    }

    private fun generateRandomName(): String {
        val adjetivos = listOf("fellow", "swift", "bold", "wild", "fast", "brave", "cool", "urban")
        val sustantivos = listOf("Sparrow", "Runner", "Tiger", "Eagle", "Falcon", "Ghost", "Striders", "Walker")
        return "${adjetivos.random()}${sustantivos.random()}${(100..999).random()}"
    }

    fun loginWithGoogle(idToken: String, onResult: (shouldGoToOnboarding: Boolean) -> Unit) {
        uiState = AuthUiState.Loading
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .addOnSuccessListener { credentialResult ->
                val currentUid = credentialResult.user?.uid ?: ""
                viewModelScope.launch {
                    val data = userRepository.getUserData(currentUid)
                    if (data == null) {
                        val firebaseUser = credentialResult.user
                        if (firebaseUser != null) {
                            val user = User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Runner",
                                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                            )
                            userRepository.saveUser(user)
                        }
                        uiState = AuthUiState.Success
                        onResult(true) // Nuevo usuario -> Onboarding
                    } else {
                        val weight = (data["weightKg"] as? Number)?.toDouble() ?: 0.0
                        val height = (data["heightCm"] as? Number)?.toInt() ?: 0
                        
                        uiState = AuthUiState.Success
                        // Si user no tiene peso o altura, debe ir al onboarding
                        onResult(weight <= 0.0 || height <= 0)
                    }
                }
            }
            .addOnFailureListener { e ->
                uiState = AuthUiState.Error(mapFirebaseError(e))
            }
    }

    fun checkUserOnboardingStatus(onResult: (shouldGoToOnboarding: Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: ""
        if (currentUid.isBlank()) {
            onResult(false)
            return
        }
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            val data = userRepository.getUserData(currentUid)
            if (data == null) {
                uiState = AuthUiState.Success
                onResult(true) 
            } else {
                val weight = (data["weightKg"] as? Number)?.toDouble() ?: 0.0
                val height = (data["heightCm"] as? Number)?.toInt() ?: 0
                uiState = AuthUiState.Success
                onResult(weight <= 0.0 || height <= 0)
            }
        }
    }

    private fun mapFirebaseError(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Email o contraseña incorrectos."
            is FirebaseAuthInvalidUserException -> "El usuario no existe."
            else -> e.localizedMessage ?: "Ocurrió un error inesperado."
        }
    }

    fun resetState() { uiState = AuthUiState.Idle }
}