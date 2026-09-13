package com.example.todoapp.domain.auth.model

data class MfaSetupData(
    val otpUri: String,
    val secretBase32: String,
    val expiresAt: String?
)