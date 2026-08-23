package com.example.todoapp.usecase.auth

import com.example.todoapp.database.repository.auth.AuthRepository
import com.example.todoapp.session.RestoreSessionResult
import javax.inject.Inject

class CheckAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): RestoreSessionResult {
        return authRepository.restoreSession()
    }
}