package com.example.todoapp.domain.auth.usecase

import com.example.todoapp.domain.auth.model.RestoreSessionResult
import com.example.todoapp.domain.auth.repository.AuthRepository
import javax.inject.Inject

class CheckAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): RestoreSessionResult {
        return authRepository.restoreSession()
    }
}