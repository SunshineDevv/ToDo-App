package com.example.todoapp.data.auth.remote.dto

data class HealthResponse(
    val status: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String? = null,
    val devResetToken: String? = null
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class ResetPasswordResponse(
    val message: String? = null
)

data class BackendUserResponse(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val mfaEnabled: Boolean = false
)

data class RegisterResponse(
    val user: BackendUserResponse?
)

data class LoginResponse(
    val mfaRequired: Boolean = false,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val loginTicket: String? = null,
    val user: BackendUserResponse? = null
)

data class RefreshResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null
)

data class CurrentUserResponse(
    val user: BackendUserResponse?
)