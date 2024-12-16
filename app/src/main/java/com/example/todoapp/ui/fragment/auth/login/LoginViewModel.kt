package com.example.todoapp.ui.fragment.auth.login

import androidx.lifecycle.ViewModel
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(

) : ViewModel() {

    private lateinit var auth: FirebaseAuth

    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")

    private val _logInState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val logInState = _logInState.asStateFlow()

    fun logInUser(email: String, password: String) {
        auth = Firebase.auth

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _logInState.value = AuthenticationState.Success
                } else {
                    when (val exception = task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            _logInState.value =
                                AuthenticationState.Error("Invalid credentials. Check your email and password.")
                        }

                        is FirebaseAuthInvalidUserException -> {
                            _logInState.value =
                                AuthenticationState.Error("User does not exist. Please register.")
                        }

                        else -> {
                            _logInState.value =
                                AuthenticationState.Error("Authentication failed: ${exception?.message}")
                        }
                    }
                }
            }
    }

    fun clearState() {
        _logInState.value = AuthenticationState.Empty
    }
}