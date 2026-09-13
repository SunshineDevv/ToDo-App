package com.example.todoapp.presentation.security.state

sealed class SecurityState {

    data object Empty : SecurityState()
    data object Loading : SecurityState()
    data class MfaSetupStarted(
        val otpUri: String,
        val secretBase32: String,
        val algorithm: String,
        val digits: Int,
        val periodSeconds: Int,
        val expiresAt: String?
    ) : SecurityState()
    data class Success(val successMsg: String) : SecurityState()
    data class Error(val errorMsg: String) : SecurityState()
}