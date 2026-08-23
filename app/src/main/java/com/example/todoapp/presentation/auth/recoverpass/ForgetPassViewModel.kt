package com.example.todoapp.presentation.auth.recoverpass

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.usecase.ForgotPassUseCase
import com.example.todoapp.presentation.auth.mapper.AuthErrorMessageMapper
import com.example.todoapp.presentation.auth.state.AuthenticationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgetPassViewModel @Inject constructor(
    private val forgotPassUseCase: ForgotPassUseCase
) : ViewModel() {

    val userEmail = MutableStateFlow("")

    private val _resetState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val resetState = _resetState.asStateFlow()

    fun resetPassword(userEmail: String) {
        val trimmedEmail = userEmail.trim()

        if (trimmedEmail.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("Email cannot be empty.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _resetState.value = AuthenticationState.ErrorReset("Invalid email format.")
            return
        }

        _resetState.value = AuthenticationState.Loading

        viewModelScope.launch {
            when (val result = forgotPassUseCase(trimmedEmail)) {
                is AppResult.Success -> {
                    val resetResult = result.data

                    Log.i("BACKEND_RESET", "password reset request completed")

                    _resetState.value = AuthenticationState.PasswordResetRequested(
                        message = resetResult.message,
                        devResetToken = resetResult.devResetToken
                    )
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_RESET", "forgot password failed: ${result.error}")

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