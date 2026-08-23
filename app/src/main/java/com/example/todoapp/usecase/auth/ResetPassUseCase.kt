package com.example.todoapp.usecase.auth

import com.example.todoapp.database.repository.auth.AuthRepository
import com.example.todoapp.network.dto.ResetPasswordResponse
import javax.inject.Inject

class ResetPassUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        token: String,
        newPassword: String
    ): ResetPasswordResponse {
        return authRepository.resetPassword(
            token = token,
            newPassword = newPassword
        )
    }
}