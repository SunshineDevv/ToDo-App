package com.example.todoapp.domain.auth.repository

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.AuthUser
import com.example.todoapp.domain.auth.model.ForgotPasswordResult
import com.example.todoapp.domain.auth.model.LoginResult
import com.example.todoapp.domain.auth.model.ResetPasswordResult
import com.example.todoapp.domain.auth.model.RestoreSessionResult

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String,
        name: String
    ): AppResult<AuthUser?, AuthError>

    suspend fun login(
        email: String,
        password: String
    ): AppResult<LoginResult, AuthError>

    suspend fun forgotPassword(
        email: String
    ): AppResult<ForgotPasswordResult, AuthError>

    suspend fun resetPassword(
        token: String,
        newPassword: String
    ): AppResult<ResetPasswordResult, AuthError>

    suspend fun restoreSession(): RestoreSessionResult

    suspend fun getCurrentUser(): AppResult<AuthUser?, AuthError>

    suspend fun logoutCurrentSession(): AppResult<Unit, AuthError>
}