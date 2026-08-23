package com.example.todoapp.presentation.auth.recoverpass

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.usecase.ResetPassUseCase
import com.example.todoapp.presentation.auth.mapper.AuthErrorMessageMapper
import com.example.todoapp.presentation.auth.state.AuthenticationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPassViewModel @Inject constructor(
    private val resetPassUseCase: ResetPassUseCase
) : ViewModel() {

    val resetToken = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _resetState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val resetState = _resetState.asStateFlow()

    fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val trimmedToken = token.trim()
        val rawNewPassword = newPassword
        val rawConfirmPassword = confirmPassword

        if (trimmedToken.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("Reset token cannot be empty.")
            return
        }

        if (rawNewPassword.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("New password cannot be empty.")
            return
        }

        if (rawConfirmPassword.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("Confirm password cannot be empty.")
            return
        }

        if (rawNewPassword != rawConfirmPassword) {
            _resetState.value = AuthenticationState.ErrorReset("Passwords do not match.")
            return
        }

        _resetState.value = AuthenticationState.Loading

        viewModelScope.launch {
            when (val result = resetPassUseCase(
                token = trimmedToken,
                newPassword = rawNewPassword
            )) {
                is AppResult.Success -> {
                    val resetResult = result.data

                    Log.i("BACKEND_RESET", "password reset completed")

                    _resetState.value = AuthenticationState.PasswordResetCompleted(
                        resetResult.message
                    )
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_RESET", "reset password failed: ${result.error}")

                    _resetState.value = AuthenticationState.ErrorReset(
                        AuthErrorMessageMapper.toMessage(result.error)
                    )
                }
            }
        }
    }

    fun clearState() {
        _resetState.value = AuthenticationState.Empty
    }
}