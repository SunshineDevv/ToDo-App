package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.MfaSetupData
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class BeginMfaSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        password: String,
        algorithm: String = "SHA256",
        currentCode: String? = null
    ): AppResult<MfaSetupData, AuthError> {
        return authRepository.beginMfaSetup(
            password = password,
            algorithm = algorithm,
            currentCode = currentCode
        )
    }
}