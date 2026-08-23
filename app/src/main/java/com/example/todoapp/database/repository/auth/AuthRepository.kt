package com.example.todoapp.database.repository.auth

import android.util.Log
import com.example.todoapp.database.local.auth.BackendTokenStorage
import com.example.todoapp.di.AuthenticatedBackendApi
import com.example.todoapp.di.PublicBackendApi
import com.example.todoapp.network.api.AuthBackendApi
import com.example.todoapp.network.dto.BackendUserResponse
import com.example.todoapp.network.dto.LoginRequest
import com.example.todoapp.network.dto.LoginResponse
import com.example.todoapp.network.dto.LogoutRequest
import com.example.todoapp.network.dto.RefreshRequest
import com.example.todoapp.network.dto.RefreshResponse
import com.example.todoapp.network.dto.RegisterRequest
import com.example.todoapp.network.dto.RegisterResponse
import com.example.todoapp.network.dto.ForgotPasswordRequest
import com.example.todoapp.network.dto.ForgotPasswordResponse
import com.example.todoapp.network.dto.ResetPasswordRequest
import com.example.todoapp.network.dto.ResetPasswordResponse
import kotlinx.coroutines.CancellationException
import com.example.todoapp.session.RestoreSessionResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @PublicBackendApi private val publicApi: AuthBackendApi,
    @AuthenticatedBackendApi private val authenticatedApi: AuthBackendApi,
    private val backendTokenStorage: BackendTokenStorage
) {

    suspend fun register(
        email: String,
        password: String,
        name: String
    ): RegisterResponse {
        return publicApi.register(
            RegisterRequest(
                email = email,
                password = password,
                name = name
            )
        )
    }

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {
        return publicApi.login(
            LoginRequest(
                email = email,
                password = password
            )
        )
    }

    suspend fun forgotPassword(
        email: String
    ): ForgotPasswordResponse {
        return publicApi.forgotPassword(
            ForgotPasswordRequest(email = email)
        )
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String
    ): ResetPasswordResponse {
        return publicApi.resetPassword(
            ResetPasswordRequest(
                token = token,
                newPassword = newPassword
            )
        )
    }

    suspend fun refresh(
        refreshToken: String
    ): RefreshResponse {
        return publicApi.refresh(
            RefreshRequest(
                refreshToken = refreshToken
            )
        )
    }

    suspend fun restoreSession(): RestoreSessionResult {
        val oldRefreshToken = backendTokenStorage.getRefreshToken()

        if (oldRefreshToken.isNullOrBlank()) {
            backendTokenStorage.clearTokens()
            Log.i("BACKEND_SESSION", "refresh token is missing")
            return RestoreSessionResult.Unauthenticated
        }

        return try {
            val response = refresh(oldRefreshToken)

            val newAccessToken = response.accessToken
            val newRefreshToken = response.refreshToken

            if (newAccessToken.isNullOrBlank() || newRefreshToken.isNullOrBlank()) {
                Log.e("BACKEND_SESSION", "refresh response does not contain tokens")
                RestoreSessionResult.ServerUnavailable
            } else {
                backendTokenStorage.saveTokens(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken
                )

                Log.i("BACKEND_SESSION", "session restored successfully")
                RestoreSessionResult.Authenticated
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> {
                    backendTokenStorage.clearTokens()
                    Log.e("BACKEND_SESSION", "refresh token rejected: HTTP ${e.code()}", e)
                    RestoreSessionResult.Unauthenticated
                }

                in 500..599 -> {
                    Log.e("BACKEND_SESSION", "auth server unavailable: HTTP ${e.code()}", e)
                    RestoreSessionResult.ServerUnavailable
                }

                else -> {
                    Log.e("BACKEND_SESSION", "unexpected refresh HTTP error: ${e.code()}", e)
                    RestoreSessionResult.ServerUnavailable
                }
            }
        } catch (e: IOException) {
            Log.e("BACKEND_SESSION", "session restore failed: connection error", e)
            RestoreSessionResult.NetworkUnavailable
        } catch (e: Exception) {
            Log.e("BACKEND_SESSION", "session restore failed: ${e.message}", e)
            RestoreSessionResult.ServerUnavailable
        }
    }

    suspend fun getCurrentUser(): BackendUserResponse? {
        return authenticatedApi.getCurrentUser().user
    }

    fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        backendTokenStorage.saveTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun hasTokens(): Boolean {
        return backendTokenStorage.hasTokens()
    }

    fun clearTokens() {
        backendTokenStorage.clearTokens()
    }

    suspend fun logoutCurrentSession() {
        val refreshToken = backendTokenStorage.getRefreshToken()

        try {
            if (!refreshToken.isNullOrBlank()) {
                publicApi.logout(
                    LogoutRequest(refreshToken = refreshToken)
                )

                Log.i("BACKEND_LOGOUT", "backend logout request completed")
            } else {
                Log.i("BACKEND_LOGOUT", "refreshToken is empty, only local tokens will be cleared")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("BACKEND_LOGOUT", "backend logout request failed: ${e.message}", e)
        } finally {
            backendTokenStorage.clearTokens()
            Log.i("BACKEND_LOGOUT", "local backend tokens cleared")
        }
    }

    suspend fun checkHealth() = publicApi.health()
}