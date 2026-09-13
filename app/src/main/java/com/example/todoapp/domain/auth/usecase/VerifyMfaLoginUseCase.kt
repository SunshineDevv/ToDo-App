package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.LoginResult
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class VerifyMfaLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        loginTicket: String,
        code: String
    ): AppResult<LoginResult.Success, AuthError> {
        return authRepository.verifyMfaLogin(
            loginTicket = loginTicket,
            code = code
        )
    }
}