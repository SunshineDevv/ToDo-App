package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.MfaStatus
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class DisableMfaUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        password: String,
        code: String
    ): AppResult<MfaStatus, AuthError> {
        return authRepository.disableMfa(
            password = password,
            code = code
        )
    }
}