package com.example.todoapp.domain.auth.model

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object InvalidEmail : AuthError
    data object EmailAlreadyExists : AuthError
    data object InvalidOrExpiredResetToken : AuthError
    data object Unauthorized : AuthError
    data object TooManyRequests : AuthError
    data object NetworkUnavailable : AuthError
    data object ServerUnavailable : AuthError
    data object EndpointNotFound : AuthError
    data object InvalidServerResponse : AuthError

    data object InvalidPassword : AuthError
    data object InvalidMfaCode : AuthError
    data object InvalidMfaChallenge : AuthError
    data object MfaSetupExpired : AuthError
    data object MfaNotConfigured : AuthError

    data class Unknown(
        val message: String? = null
    ) : AuthError
}