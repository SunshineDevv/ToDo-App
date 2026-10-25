package com.example.todoapp.domain.device.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.device.model.TrustedDeviceError
import com.example.todoapp.domain.device.model.TrustedDeviceState
import com.example.todoapp.domain.device.repository.TrustedDeviceRepository
import javax.inject.Inject

class BindTrustedDeviceUseCase @Inject constructor(
    private val trustedDeviceRepository:
    TrustedDeviceRepository
) {

    suspend operator fun invoke():
            AppResult<TrustedDeviceState, TrustedDeviceError> {

        return trustedDeviceRepository
            .bindCurrentSession()
    }
}