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
import com.example.todoapp.data.auth.remote.dto.VerifyMfaLoginResponse
import com.example.todoapp.data.auth.remote.dto.MfaSetupBeginResponse
import com.example.todoapp.data.auth.remote.dto.MfaStatusResponse
import com.example.todoapp.domain.auth.model.MfaSetupData
import com.example.todoapp.domain.auth.model.MfaStatus

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
            val ticket = loginTicket?.takeIf { it.isNotBlank() } ?: return null

            return LoginResult.MfaRequired(
                loginTicket = ticket
            )
        }

        val tokenPair = toTokenPair() ?: return null
        val authUser = user?.toDomain() ?: return null

        return LoginResult.Success(
            user = authUser,
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

    fun VerifyMfaLoginResponse.toTokenPair(): TokenPair? {
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

    fun VerifyMfaLoginResponse.toDomain(): LoginResult.Success? {
        val tokenPair = toTokenPair()
        val authUser = user?.toDomain()

        if (tokenPair == null || authUser == null) {
            return null
        }

        return LoginResult.Success(
            user = authUser,
            tokens = tokenPair
        )
    }

    fun MfaSetupBeginResponse.toDomain(): MfaSetupData? {
        val uri = otpUri?.takeIf { it.isNotBlank() } ?: return null
        val secret = secretBase32?.takeIf { it.isNotBlank() } ?: return null

        return MfaSetupData(
            otpUri = uri,
            secretBase32 = secret,
            algorithm = algorithm?.takeIf { it.isNotBlank() } ?: "SHA256",
            digits = digits ?: 6,
            periodSeconds = periodSeconds ?: 30,
            expiresAt = expiresAt
        )
    }

    fun MfaStatusResponse.toDomain(): MfaStatus? {
        val status = enabled ?: return null

        return MfaStatus(
            enabled = status
        )
    }
}