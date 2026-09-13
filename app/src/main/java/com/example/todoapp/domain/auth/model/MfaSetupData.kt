package com.example.todoapp.domain.auth.model

data class MfaSetupData(
    val otpUri: String,
    val secretBase32: String,
    val algorithm: String,
    val digits: Int,
    val periodSeconds: Int,
    val expiresAt: String?
)