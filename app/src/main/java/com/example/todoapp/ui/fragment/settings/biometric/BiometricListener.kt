package com.example.todoapp.ui.fragment.settings.biometric

import androidx.biometric.BiometricPrompt

interface BiometricListener {
    fun onBiometricAuthenticateError(error: Int,errMsg: String)
    fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult)
    fun onBiometricAuthenticateFailed(failedMsg: String)
}