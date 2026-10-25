package com.example.todoapp.domain.device.repository

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.device.model.TrustedDeviceError
import com.example.todoapp.domain.device.model.TrustedDeviceState

interface TrustedDeviceRepository {

    suspend fun getStateForUser(
        userId: String
    ): AppResult<TrustedDeviceState, TrustedDeviceError>

    suspend fun enroll(
        password: String,
        currentTotp: String?
    ): AppResult<TrustedDeviceState, TrustedDeviceError>

    suspend fun bindCurrentSession():
            AppResult<TrustedDeviceState, TrustedDeviceError>

    suspend fun revokeCurrentDevice():
            AppResult<Unit, TrustedDeviceError>
}