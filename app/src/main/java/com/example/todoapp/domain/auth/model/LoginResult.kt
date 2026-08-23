package com.example.todoapp.domain.auth.model

sealed interface LoginResult {

    data class Success(
        val user: AuthUser?,
        val tokens: TokenPair
    ) : LoginResult

    data class MfaRequired(
        val loginTicket: String
    ) : LoginResult
}