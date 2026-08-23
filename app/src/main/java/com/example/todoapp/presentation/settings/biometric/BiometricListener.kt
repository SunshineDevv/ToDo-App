package com.example.todoapp.presentation.settings.biometric

import androidx.biometric.BiometricPrompt

interface BiometricListener {
    fun onBiometricAuthenticateError(error: Int,errMsg: String)
    fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult)
    fun onBiometricAuthenticateFailed(failedMsg: String)
}