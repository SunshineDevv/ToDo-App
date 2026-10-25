package com.example.todoapp.data.device.remote.dto

data class DeviceChallengeResponse(
    val challengeId: String,
    val challenge: String,
    val purpose: String,
    val userId: String,
    val sessionId: String,
    val deviceId: String?,
    val expiresAt: String
)