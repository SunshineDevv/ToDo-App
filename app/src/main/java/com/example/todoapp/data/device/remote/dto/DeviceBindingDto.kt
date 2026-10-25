package com.example.todoapp.data.device.remote.dto

data class BindChallengeRequest(
    val deviceId: String
)

data class BindConfirmRequest(
    val deviceId: String,
    val challengeId: String,
    val challenge: String,
    val signature: String
)

data class CurrentBindingResponse(
    val deviceBound: Boolean,
    val trustedDeviceId: String?
)