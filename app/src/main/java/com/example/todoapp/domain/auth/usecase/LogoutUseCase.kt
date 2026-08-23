package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): AppResult<Unit, AuthError> {
        return authRepository.logoutCurrentSession()
    }
}