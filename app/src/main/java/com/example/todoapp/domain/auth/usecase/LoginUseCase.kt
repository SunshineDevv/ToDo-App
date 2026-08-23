package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.LoginResult
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ): AppResult<LoginResult, AuthError> {
        val result = authRepository.login(
            email = email,
            password = password
        )

        if (result is AppResult.Success) {
            when (val loginResult = result.data) {
                is LoginResult.Success -> {
                    authRepository.saveTokens(loginResult.tokens)
                }

                is LoginResult.MfaRequired -> {
                    // Tokens are not issued yet. User must pass MFA first.
                }
            }
        }

        return result
    }
}