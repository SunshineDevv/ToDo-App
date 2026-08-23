package com.example.todoapp.presentation.auth.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.usecase.RegisterUseCase
import com.example.todoapp.presentation.auth.mapper.AuthErrorMessageMapper
import com.example.todoapp.presentation.auth.state.AuthenticationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    val userName = MutableStateFlow("")
    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")
    val userConfirmPassword = MutableStateFlow("")

    private val _registrationState =
        MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val registrationState = _registrationState.asStateFlow()

    fun registerNewUser(
        email: String,
        password: String,
        name: String,
        confirmPassword: String
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val rawPassword = password
        val rawConfirmPassword = confirmPassword

        if (
            trimmedName.isBlank() &&
            trimmedEmail.isBlank() &&
            rawPassword.isBlank() &&
            rawConfirmPassword.isBlank()
        ) {
            _registrationState.value = AuthenticationState.Error("Credentials cannot be empty.")
            return
        }

        if (trimmedName.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Name field cannot be empty.")
            return
        }

        if (trimmedEmail.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Email field cannot be empty.")
            return
        }

        if (rawPassword.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Password field cannot be empty.")
            return
        }

        if (rawConfirmPassword.isBlank()) {
            _registrationState.value =
                AuthenticationState.Error("Confirming field cannot be empty.")
            return
        }

        if (rawPassword != rawConfirmPassword) {
            _registrationState.value = AuthenticationState.Error("Passwords do not match.")
            return
        }

        _registrationState.value = AuthenticationState.Loading

        viewModelScope.launch {
            when (val result = registerUseCase(
                email = trimmedEmail,
                password = rawPassword,
                name = trimmedName
            )) {
                is AppResult.Success -> {
                    Log.i("BACKEND_REGISTER", "registration completed")
                    _registrationState.value = AuthenticationState.Success
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_REGISTER", "registration failed: ${result.error}")

                    _registrationState.value = AuthenticationState.Error(
                        AuthErrorMessageMapper.toMessage(result.error)
                    )
                }
            }
        }
    }

    fun clearState() {
        _registrationState.value = AuthenticationState.Empty
    }
}