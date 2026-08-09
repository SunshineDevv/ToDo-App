package com.example.todoapp.network.dto

data class HealthResponse(
    val status: String
)

data class RegisterRequest(
    val email: String,
    val password: String
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

data class BackendUserResponse(
    val id: String?,
    val email: String?
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