package com.example.todoapp.data.device.remote.dto

data class EnrollmentConfirmResponse(
    val device: EnrolledDeviceDto
)

data class EnrolledDeviceDto(
    val id: String,
    val label: String,
    val createdAt: String
)