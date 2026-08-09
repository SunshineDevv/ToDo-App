package com.example.todoapp.usecase.auth

import com.example.todoapp.database.repository.auth.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ) {
        val response = authRepository.login(
            email = email,
            password = password
        )

        if (response.mfaRequired) {
            throw IllegalStateException(
                "MFA is required, but backend MFA screen is not connected yet."
            )
        }

        val accessToken = response.accessToken
        val refreshToken = response.refreshToken

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            throw IllegalStateException(
                "Backend did not return authentication tokens."
            )
        }

        authRepository.saveTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}