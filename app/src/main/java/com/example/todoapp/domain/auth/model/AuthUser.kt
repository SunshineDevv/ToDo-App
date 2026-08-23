package com.example.todoapp.domain.auth.model

data class AuthUser(
    val id: String,
    val email: String,
    val name: String?,
    val mfaEnabled: Boolean
)