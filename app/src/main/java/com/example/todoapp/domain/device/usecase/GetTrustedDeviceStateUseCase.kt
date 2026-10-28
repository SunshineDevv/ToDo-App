package com.example.todoapp.domain.device.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.usecase.GetCurrentUserUseCase
import com.example.todoapp.domain.device.model.TrustedDeviceError
import com.example.todoapp.domain.device.model.TrustedDeviceState
import com.example.todoapp.domain.device.repository.TrustedDeviceRepository
import javax.inject.Inject

class GetTrustedDeviceStateUseCase @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val trustedDeviceRepository:
    TrustedDeviceRepository
) {

    suspend operator fun invoke():
            AppResult<TrustedDeviceState, TrustedDeviceError> {

        return when (
            val currentUser =
                getCurrentUserUseCase()
        ) {

            is AppResult.Success -> {
                trustedDeviceRepository.getStateForUser(currentUser.data.id)
            }

            is AppResult.Failure -> {

                AppResult.Failure(
                    TrustedDeviceError
                        .CurrentUserUnavailable
                )
            }
        }
    }
}