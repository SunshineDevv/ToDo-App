package com.example.todoapp.presentation.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.presentation.auth.state.AuthenticationState
import com.example.todoapp.domain.auth.usecase.LoginUseCase
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.LoginResult
import com.example.todoapp.presentation.auth.mapper.AuthErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")

    private val _logInState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val logInState = _logInState.asStateFlow()

    fun logInUser(email: String, password: String) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank()) {
            _logInState.value = AuthenticationState.Error("Email cannot be empty.")
            return
        }

        if (password.isBlank()) {
            _logInState.value = AuthenticationState.Error("Password cannot be empty.")
            return
        }

        _logInState.value = AuthenticationState.Loading

        viewModelScope.launch {
            when (val result = loginUseCase(
                email = trimmedEmail,
                password = password
            )) {
                is AppResult.Success -> {
                    when (val loginResult = result.data) {
                        is LoginResult.Success -> {
                            Log.i("BACKEND_AUTH", "login completed and tokens saved")
                            _logInState.value = AuthenticationState.SuccessNoSecureEnable
                        }

                        is LoginResult.MfaRequired -> {
                            Log.i("BACKEND_AUTH", "MFA is required")
                            _logInState.value = AuthenticationState.Error(
                                "MFA is required but Android MFA flow is not connected yet."
                            )
                        }
                    }
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_AUTH", "login failed: ${result.error}")

                    _logInState.value = AuthenticationState.Error(
                        AuthErrorMessageMapper.toMessage(result.error)
                    )
                }
            }
        }
    }

    fun clearState() {
        _logInState.value = AuthenticationState.Empty
    }
}