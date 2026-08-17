package com.example.todoapp.usecase

import com.example.todoapp.database.repository.auth.AuthRepository
import com.example.todoapp.network.dto.BackendUserResponse
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): BackendUserResponse? {
        return authRepository.getCurrentUser()
    }
}