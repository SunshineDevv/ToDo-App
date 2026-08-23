package com.example.todoapp.presentation.auth.mapper

import com.example.todoapp.domain.auth.model.AuthError

object AuthErrorMessageMapper {

    fun toMessage(error: AuthError): String {
        return when (error) {
            AuthError.InvalidCredentials -> "Invalid email or password."
            AuthError.InvalidEmail -> "Invalid email."
            AuthError.EmailAlreadyExists -> "User with this email already exists."
            AuthError.InvalidOrExpiredResetToken -> "Reset token is invalid or expired."
            AuthError.Unauthorized -> "Session expired. Please log in again."
            AuthError.TooManyRequests -> "Too many attempts. Please wait and try again."
            AuthError.NetworkUnavailable -> "Cannot connect to authentication server."
            AuthError.ServerUnavailable -> "Authentication server is temporarily unavailable."
            AuthError.EndpointNotFound -> "Authentication endpoint was not found."
            AuthError.InvalidServerResponse -> "Authentication server returned invalid response."
            is AuthError.Unknown -> "Authentication request failed."        }
    }
}