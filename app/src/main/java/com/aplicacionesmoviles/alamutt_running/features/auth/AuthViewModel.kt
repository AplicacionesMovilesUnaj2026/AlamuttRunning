package com.aplicacionesmoviles.alamutt_running.features.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
            .addOnSuccessListener {
                uiState = AuthUiState.Success
                onSuccess()
            }
            .addOnFailureListener { e ->
                uiState = AuthUiState.Error(mapFirebaseError(e))
            }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        uiState = AuthUiState.Loading
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .addOnSuccessListener {
                uiState = AuthUiState.Success
                onSuccess()
            }
            .addOnFailureListener { e ->
                uiState = AuthUiState.Error(mapFirebaseError(e))
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