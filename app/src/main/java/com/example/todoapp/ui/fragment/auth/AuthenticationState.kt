package com.example.todoapp.ui.fragment.auth

sealed class AuthenticationState {
    data object SuccessNewUser : AuthenticationState()
    data object Success : AuthenticationState()
    data object SuccessNoSecureEnable : AuthenticationState()
    data object SuccessWithSecureEnable: AuthenticationState()
    data object Empty : AuthenticationState()
    data class Error(val errorMsg: String) : AuthenticationState()
    data class FatalError(val errorMsg: String) : AuthenticationState()
    data class SuccessReset(val successMsg: String) : AuthenticationState()
    data class ErrorReset(val errorMsg: String) : AuthenticationState()
}