package com.example.todoapp.data.device.remote.dto

data class EnrollmentConfirmRequest(
    val challengeId: String,
    val challenge: String,
    val signature: String,
    val publicKey: String,
    val label: String,
    val password: String,
    val currentTotp: String? = null
)