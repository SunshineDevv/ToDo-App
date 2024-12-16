package com.example.todoapp.ui.fragment.auth

sealed class AuthenticationState {
    data object Success : AuthenticationState()
    data object Empty : AuthenticationState()
    data class Error(val errorMsg: String) : AuthenticationState()
}