package com.example.todoapp.usecase.auth

import com.example.todoapp.database.repository.auth.AuthRepository
import com.example.todoapp.network.dto.ForgotPasswordResponse
import javax.inject.Inject

class ForgotPassUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String
    ): ForgotPasswordResponse {
        return authRepository.forgotPassword(email = email)
    }
}