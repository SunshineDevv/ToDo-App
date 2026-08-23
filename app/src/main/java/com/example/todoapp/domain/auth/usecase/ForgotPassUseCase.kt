package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.ForgotPasswordResult
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class ForgotPassUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String
    ): AppResult<ForgotPasswordResult, AuthError> {
        return authRepository.forgotPassword(email = email)
    }
}