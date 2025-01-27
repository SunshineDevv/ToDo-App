package com.example.todoapp.ui.fragment.security.securitystate

sealed class SecurityState {
    data class Success(val successMsg: String) : SecurityState()
    data class Error(val errorMsg: String?) : SecurityState()
    data class LoadingData(val secret: String, val base32secret: String, val otpUri: String) : SecurityState()
    data object Empty : SecurityState()
}