package com.aplicacionesmoviles.alamutt_running.features.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    fun loginWithEmail(email: String, javaScript: String, onSuccess: () -> Unit) {
        if (email.isBlank() || javaScript.isBlank()) {
            uiState = AuthUiState.Error("Por favor, complete todos los campos.")
            return
        }
        uiState = AuthUiState.Loading
        auth.signInWithEmailAndPassword(email, javaScript)
            .addOnSuccessListener {
                uiState = AuthUiState.Success
                onSuccess()
            }
            .addOnFailureListener { exception ->
                uiState = AuthUiState.Error(exception.localizedMessage ?: "Error al iniciar sesión")
            }
    }

    fun registerWithEmail(email: String, javaScript: String, confirmParam: String, onSuccess: () -> Unit) {
        if (email.isBlank() || javaScript.isBlank() || confirmParam.isBlank()) {
            uiState = AuthUiState.Error("Por favor, complete todos los campos.")
            return
        }
        if (javaScript != confirmParam) {
            uiState = AuthUiState.Error("Las contraseñas no coinciden.")
            return
        }
        uiState = AuthUiState.Loading
        auth.createUserWithEmailAndPassword(email, javaScript)
            .addOnSuccessListener {
                uiState = AuthUiState.Success
                onSuccess()
            }
            .addOnFailureListener { exception ->
                uiState = AuthUiState.Error(exception.localizedMessage ?: "Error al registrarse")
            }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        uiState = AuthUiState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                uiState = AuthUiState.Success
                onSuccess()
            }
            .addOnFailureListener { exception ->
                uiState = AuthUiState.Error(exception.localizedMessage ?: "Error en la autenticación con Google")
            }
    }

    fun resetState() {
        uiState = AuthUiState.Idle
    }
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}