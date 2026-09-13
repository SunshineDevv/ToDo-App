package com.example.todoapp.domain.auth.repository

import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.AuthUser
import com.example.todoapp.domain.auth.model.ForgotPasswordResult
import com.example.todoapp.domain.auth.model.LoginResult
import com.example.todoapp.domain.auth.model.ResetPasswordResult
import com.example.todoapp.domain.auth.model.RestoreSessionResult
import com.example.todoapp.domain.auth.model.MfaSetupData
import com.example.todoapp.domain.auth.model.MfaStatus

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String,
        name: String
    ): AppResult<AuthUser, AuthError>

    suspend fun login(
        email: String,
        password: String
    ): AppResult<LoginResult, AuthError>

    suspend fun verifyMfaLogin(
        loginTicket: String,
        code: String
    ): AppResult<LoginResult.Success, AuthError>

    suspend fun beginMfaSetup(
        password: String,
        currentCode: String? = null
    ): AppResult<MfaSetupData, AuthError>

    suspend fun confirmMfaSetup(
        code: String
    ): AppResult<MfaStatus, AuthError>

    suspend fun disableMfa(
        password: String,
        code: String
    ): AppResult<MfaStatus, AuthError>

    suspend fun forgotPassword(
        email: String
    ): AppResult<ForgotPasswordResult, AuthError>

    suspend fun resetPassword(
        token: String,
        newPassword: String
    ): AppResult<ResetPasswordResult, AuthError>

    suspend fun restoreSession(): RestoreSessionResult

    suspend fun getCurrentUser(): AppResult<AuthUser, AuthError>

    suspend fun logoutCurrentSession(): AppResult<Unit, AuthError>
}