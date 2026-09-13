package com.example.todoapp.presentation.security

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.usecase.BeginMfaSetupUseCase
import com.example.todoapp.domain.auth.usecase.ConfirmMfaSetupUseCase
import com.example.todoapp.domain.auth.usecase.DisableMfaUseCase
import com.example.todoapp.domain.auth.usecase.GetCurrentUserUseCase
import com.example.todoapp.presentation.auth.mapper.AuthErrorMessageMapper
import com.example.todoapp.presentation.security.state.SecurityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val beginMfaSetupUseCase: BeginMfaSetupUseCase,
    private val confirmMfaSetupUseCase: ConfirmMfaSetupUseCase,
    private val disableMfaUseCase: DisableMfaUseCase
) : ViewModel() {

    private val _securityState = MutableStateFlow<SecurityState>(SecurityState.Empty)
    val securityState = _securityState.asStateFlow()

    private val _isMfaEnabled = MutableStateFlow<Boolean?>(null)
    val isMfaEnabled = _isMfaEnabled.asStateFlow()

    private val _selectedAlgorithm = MutableStateFlow(DEFAULT_ALGORITHM)
    val selectedAlgorithm = _selectedAlgorithm.asStateFlow()

    fun onStart() {
        _isMfaEnabled.value = null

        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    _isMfaEnabled.value = result.data.mfaEnabled
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_MFA", "Failed to load MFA status: ${result.error}")
                    _securityState.value = SecurityState.Error(
                        AuthErrorMessageMapper.toMessage(result.error)
                    )
                }
            }
        }
    }

    fun setAlgorithm(algorithm: String) {
        if (algorithm !in ALLOWED_ALGORITHMS) {
            _securityState.value = SecurityState.Error("Unsupported MFA algorithm.")
            return
        }

        _selectedAlgorithm.value = algorithm
    }

    fun beginMfaSetup(
        password: String,
        currentCode: String? = null
    ) {
        val mfaEnabled = _isMfaEnabled.value

        if (mfaEnabled == null) {
            _securityState.value = SecurityState.Error("MFA status is still loading.")
            return
        }

        if (password.isBlank()) {
            _securityState.value = SecurityState.Error("Password cannot be empty.")
            return
        }

        val normalizedCurrentCode = currentCode
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (mfaEnabled) {
            if (normalizedCurrentCode == null) {
                _securityState.value = SecurityState.Error("Current MFA code cannot be empty.")
                return
            }

            if (!MFA_CODE_REGEX.matches(normalizedCurrentCode)) {
                _securityState.value = SecurityState.Error("Current MFA code must contain 6 digits.")
                return
            }
        }

        _securityState.value = SecurityState.Loading

        viewModelScope.launch {
            when (
                val result = beginMfaSetupUseCase(
                    password = password,
                    algorithm = _selectedAlgorithm.value,
                    currentCode = if (mfaEnabled) normalizedCurrentCode else null
                )
            ) {
                is AppResult.Success -> {
                    val setupData = result.data

                    _securityState.value = SecurityState.MfaSetupStarted(
                        otpUri = setupData.otpUri,
                        secretBase32 = setupData.secretBase32,
                        algorithm = setupData.algorithm,
                        digits = setupData.digits,
                        periodSeconds = setupData.periodSeconds,
                        expiresAt = setupData.expiresAt
                    )
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_MFA", "MFA setup begin failed: ${result.error}")
                    _securityState.value = SecurityState.Error(
                        AuthErrorMessageMapper.toMessage(result.error)
                    )
                }
            }
        }
    }

    fun confirmMfaSetup(code: String) {
        val trimmedCode = code.trim()

        if (trimmedCode.isBlank()) {
            _securityState.value = SecurityState.Error("Authentication code cannot be empty.")
            return
        }

        if (!MFA_CODE_REGEX.matches(trimmedCode)) {
            _securityState.value = SecurityState.Error("Authentication code must contain 6 digits.")
            return
        }

        _securityState.value = SecurityState.Loading

        viewModelScope.launch {
            when (val result = confirmMfaSetupUseCase(code = trimmedCode)) {
                is AppResult.Success -> {
                    _isMfaEnabled.value = result.data.enabled
                    _securityState.value = SecurityState.Success("MFA enabled successfully.")
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_MFA", "MFA setup confirm failed: ${result.error}")
                    _securityState.value = SecurityState.Error(
                        AuthErrorMessageMapper.toMessage(result.error)
                    )
                }
            }
        }
    }

    fun disableMfa(password: String, code: String) {
        val rawPassword = password
        val trimmedCode = code.trim()

        if (rawPassword.isBlank()) {
            _securityState.value = SecurityState.Error("Password cannot be empty.")
            return
        }

        if (trimmedCode.isBlank()) {
            _securityState.value = SecurityState.Error("Authentication code cannot be empty.")
            return
        }

        if (!MFA_CODE_REGEX.matches(trimmedCode)) {
            _securityState.value = SecurityState.Error("Authentication code must contain 6 digits.")
            return
        }

        _securityState.value = SecurityState.Loading

        viewModelScope.launch {
            when (
                val result = disableMfaUseCase(
                    password = rawPassword,
                    code = trimmedCode
                )
            ) {
                is AppResult.Success -> {
                    _isMfaEnabled.value = result.data.enabled
                    _securityState.value = SecurityState.Success("MFA disabled successfully.")
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_MFA", "MFA disable failed: ${result.error}")
                    _securityState.value = SecurityState.Error(
                        AuthErrorMessageMapper.toMessage(result.error)
                    )
                }
            }
        }
    }

    fun clearState() {
        _securityState.value = SecurityState.Empty
    }

    private companion object {
        const val DEFAULT_ALGORITHM = "SHA256"

        val ALLOWED_ALGORITHMS = setOf(
            "SHA1",
            "SHA256",
            "SHA512"
        )

        val MFA_CODE_REGEX = Regex("^\\d{6}$")
    }
}