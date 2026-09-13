package com.example.todoapp.presentation.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.usecase.VerifyMfaLoginUseCase
import com.example.todoapp.presentation.auth.mapper.AuthErrorMessageMapper
import com.example.todoapp.presentation.auth.state.AuthenticationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwoAuthVIewModel @Inject constructor(
    private val verifyMfaLoginUseCase: VerifyMfaLoginUseCase
) : ViewModel() {

    val inputToken = MutableStateFlow("")

    private val _twoAuthState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val twoAuthState = _twoAuthState.asStateFlow()

    fun validateUserInputCode(
        loginTicket: String,
        userInputCode: String
    ) {
        val trimmedLoginTicket = loginTicket.trim()
        val trimmedCode = userInputCode.trim()

        if (trimmedLoginTicket.isBlank()) {
            _twoAuthState.value = AuthenticationState.FatalError(
                "MFA session expired. Please log in again."
            )
            return
        }

        if (trimmedCode.isBlank()) {
            _twoAuthState.value = AuthenticationState.Error(
                "Authentication code cannot be empty."
            )
            return
        }

        if (!MFA_CODE_REGEX.matches(trimmedCode)) {
            _twoAuthState.value = AuthenticationState.Error(
                "Authentication code must contain 6 digits."
            )
            return
        }

        _twoAuthState.value = AuthenticationState.Loading

        viewModelScope.launch {
            when (
                val result = verifyMfaLoginUseCase(
                    loginTicket = trimmedLoginTicket,
                    code = trimmedCode
                )
            ) {
                is AppResult.Success -> {
                    Log.i("BACKEND_MFA", "MFA login completed")
                    _twoAuthState.value = AuthenticationState.Success
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_MFA", "MFA login failed: ${result.error}")

                    val message = AuthErrorMessageMapper.toMessage(result.error)

                    _twoAuthState.value = when (result.error) {
                        AuthError.InvalidMfaChallenge -> {
                            AuthenticationState.FatalError(message)
                        }

                        else -> {
                            AuthenticationState.Error(message)
                        }
                    }
                }
            }
        }
    }

    fun clearState() {
        _twoAuthState.value = AuthenticationState.Empty
    }

    private companion object {
        val MFA_CODE_REGEX = Regex("^\\d{6}$")
    }
}