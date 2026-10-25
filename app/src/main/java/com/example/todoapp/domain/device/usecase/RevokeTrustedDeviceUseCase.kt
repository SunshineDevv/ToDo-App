package com.example.todoapp.domain.device.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.usecase.LogoutUseCase
import com.example.todoapp.domain.device.model.TrustedDeviceError
import com.example.todoapp.domain.device.repository.TrustedDeviceRepository
import javax.inject.Inject

class RevokeTrustedDeviceUseCase @Inject constructor(
    private val trustedDeviceRepository:
    TrustedDeviceRepository,
    private val logoutUseCase:
    LogoutUseCase
) {

    suspend operator fun invoke():
            AppResult<Unit, TrustedDeviceError> {

        val result =
            trustedDeviceRepository
                .revokeCurrentDevice()

        if (result is AppResult.Success) {

            logoutUseCase()
        }

        return result
    }
}