package com.example.todoapp.usecase.auth

import com.example.todoapp.database.repository.auth.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke() {
        authRepository.logoutCurrentSession()
    }
}