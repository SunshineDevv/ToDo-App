package com.example.todoapp.ui.fragment.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.model.UserDb
import com.example.todoapp.database.repository.UserRepository
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: UserRepository,
) : ViewModel() {

    private lateinit var auth: FirebaseAuth

    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")

    private val _logInState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val logInState = _logInState.asStateFlow()

    fun logInUser(email: String, password: String) {
        auth = Firebase.auth
        if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        userId?.let {
                            viewModelScope.launch {
                                if (repository.isUserExists(userId) == 1) {
                                    val secure = repository.isUserSecure(userId)
                                    if (secure == 0) {
                                        SecurePreferencesHelper.saveSuccess(context, "")
                                        _logInState.value =
                                            AuthenticationState.SuccessNoSecureEnable
                                    } else {
                                        SecurePreferencesHelper.saveSuccess(context, "false")
                                        _logInState.value =
                                            AuthenticationState.SuccessWithSecureEnable
                                    }
                                } else {
                                    repository.upsert(
                                        UserDb(
                                            userId = userId,
                                            userImg = null,
                                            securityEnabled = false
                                        )
                                    )
                                    SecurePreferencesHelper.saveSuccess(context, "")
                                    _logInState.value = AuthenticationState.SuccessNewUser
                                }
                            }
                        }
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
        } else if (email.isEmpty() && password.isEmpty()) {
            _logInState.value = AuthenticationState.Error("Credential fields cannot be empty")
        } else if (password.isEmpty()) {
            _logInState.value = AuthenticationState.Error("Password field cannot be empty")
        } else {
            _logInState.value = AuthenticationState.Error("Email field cannot be empty")
        }
    }

    fun clearState() {
        _logInState.value = AuthenticationState.Empty
    }
}