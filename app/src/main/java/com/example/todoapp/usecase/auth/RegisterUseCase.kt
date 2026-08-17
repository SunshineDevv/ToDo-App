package com.example.todoapp.usecase.auth

import com.example.todoapp.database.repository.auth.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String,
        name: String
    ) {
        authRepository.register(
            email = email,
            password = password,
            name = name
        )
    }
}