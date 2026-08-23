package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.ResetPasswordResult
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class ResetPassUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        token: String,
        newPassword: String
    ): AppResult<ResetPasswordResult, AuthError> {
        return authRepository.resetPassword(
            token = token,
            newPassword = newPassword
        )
    }
}