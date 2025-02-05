package com.example.todoapp.ui.fragment.auth.recoverpass

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ForgetPassViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val userEmail = MutableStateFlow("")

    private val auth = FirebaseAuth.getInstance()

    private val _resetState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val resetState = _resetState.asStateFlow()

    fun resetPassword(userEmail: String) {
        if (userEmail.isBlank()) {
            _resetState.value = AuthenticationState.Error("Email cannot be empty!")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            _resetState.value = AuthenticationState.Error("Invalid email format!")
            return
        }

        auth.sendPasswordResetEmail(userEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resetState.value =
                        AuthenticationState.SuccessReset("Password reset email sent successfully! Check your mail!")
                } else {
                    val errorMessage =
                        task.exception?.localizedMessage ?: "Failed to reset password!"
                    _resetState.value = AuthenticationState.ErrorReset(errorMessage)
                }
            }
    }

    fun clearState() {
        _resetState.value = AuthenticationState.Empty
    }
}