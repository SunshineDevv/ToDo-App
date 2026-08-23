package com.example.todoapp.data.auth.mapper

import com.example.todoapp.data.auth.remote.dto.BackendUserResponse
import com.example.todoapp.data.auth.remote.dto.ForgotPasswordResponse
import com.example.todoapp.data.auth.remote.dto.LoginResponse
import com.example.todoapp.data.auth.remote.dto.RefreshResponse
import com.example.todoapp.data.auth.remote.dto.ResetPasswordResponse
import com.example.todoapp.domain.auth.model.AuthUser
import com.example.todoapp.domain.auth.model.ForgotPasswordResult
import com.example.todoapp.domain.auth.model.LoginResult
import com.example.todoapp.domain.auth.model.ResetPasswordResult
import com.example.todoapp.domain.auth.model.TokenPair

object AuthDtoMapper {

    fun BackendUserResponse.toDomain(): AuthUser? {
        val userId = id ?: return null
        val userEmail = email ?: return null

        return AuthUser(
            id = userId,
            email = userEmail,
            name = name,
            mfaEnabled = mfaEnabled
        )
    }

    fun LoginResponse.toDomain(): LoginResult? {
        if (mfaRequired) {
            val ticket = loginTicket ?: return null

            return LoginResult.MfaRequired(
                loginTicket = ticket
            )
        }

        val tokenPair = toTokenPair() ?: return null

        return LoginResult.Success(
            user = user?.toDomain(),
            tokens = tokenPair
        )
    }

    fun LoginResponse.toTokenPair(): TokenPair? {
        val access = accessToken ?: return null
        val refresh = refreshToken ?: return null

        if (access.isBlank() || refresh.isBlank()) {
            return null
        }

        return TokenPair(
            accessToken = access,
            refreshToken = refresh
        )
    }

    fun RefreshResponse.toTokenPair(): TokenPair? {
        val access = accessToken ?: return null
        val refresh = refreshToken ?: return null

        if (access.isBlank() || refresh.isBlank()) {
            return null
        }

        return TokenPair(
            accessToken = access,
            refreshToken = refresh
        )
    }

    fun ForgotPasswordResponse.toDomain(): ForgotPasswordResult {
        return ForgotPasswordResult(
            message = message
                ?: "If an account with this email exists, password reset instructions have been sent.",
            devResetToken = devResetToken
        )
    }

    fun ResetPasswordResponse.toDomain(): ResetPasswordResult {
        return ResetPasswordResult(
            message = message ?: "Password has been reset successfully."
        )
    }
}