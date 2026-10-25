package com.example.todoapp.domain.device.model

data class TrustedDeviceState(
    val enrolled: Boolean,
    val bound: Boolean,
    val deviceId: String?,
    val verified: Boolean = true
)