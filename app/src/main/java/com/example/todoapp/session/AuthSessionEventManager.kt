package com.example.todoapp.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthSessionState {
    data object Authenticated : AuthSessionState
    data object Unauthenticated : AuthSessionState
}

@Singleton
class AuthSessionEventManager @Inject constructor() {

    private val _sessionState = MutableStateFlow<AuthSessionState>(
        AuthSessionState.Authenticated
    )

    val sessionState: StateFlow<AuthSessionState> = _sessionState.asStateFlow()

    fun markAuthenticated() {
        _sessionState.value = AuthSessionState.Authenticated
    }

    fun notifySessionExpired() {
        _sessionState.value = AuthSessionState.Unauthenticated
    }
}