package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.AuthUser
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String,
        name: String
    ): AppResult<AuthUser?, AuthError> {
        return authRepository.register(
            email = email,
            password = password,
            name = name
        )
    }
}