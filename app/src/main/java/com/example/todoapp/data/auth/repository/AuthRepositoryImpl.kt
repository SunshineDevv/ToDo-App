package com.example.todoapp.data.auth.repository

import android.util.Log
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.data.auth.local.BackendTokenStorage
import com.example.todoapp.data.auth.mapper.AuthDtoMapper.toDomain
import com.example.todoapp.data.auth.mapper.AuthDtoMapper.toTokenPair
import com.example.todoapp.data.auth.mapper.AuthExceptionMapper
import com.example.todoapp.data.auth.remote.api.AuthBackendApi
import com.example.todoapp.data.auth.remote.dto.ForgotPasswordRequest
import com.example.todoapp.data.auth.remote.dto.LoginRequest
import com.example.todoapp.data.auth.remote.dto.LogoutRequest
import com.example.todoapp.data.auth.remote.dto.RefreshRequest
import com.example.todoapp.data.auth.remote.dto.RegisterRequest
import com.example.todoapp.data.auth.remote.dto.ResetPasswordRequest
import com.example.todoapp.di.AuthenticatedBackendApi
import com.example.todoapp.di.PublicBackendApi
import com.example.todoapp.domain.auth.model.AuthError
import com.example.todoapp.domain.auth.model.AuthUser
import com.example.todoapp.domain.auth.model.ForgotPasswordResult
import com.example.todoapp.domain.auth.model.LoginResult
import com.example.todoapp.domain.auth.model.ResetPasswordResult
import com.example.todoapp.domain.auth.model.RestoreSessionResult
import com.example.todoapp.domain.auth.model.TokenPair
import com.example.todoapp.domain.auth.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @PublicBackendApi private val publicApi: AuthBackendApi,
    @AuthenticatedBackendApi private val authenticatedApi: AuthBackendApi,
    private val backendTokenStorage: BackendTokenStorage
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): AppResult<AuthUser?, AuthError> {
        return try {
            val response = publicApi.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    name = name
                )
            )

            AppResult.Success(response.user?.toDomain())

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AuthExceptionMapper.mapRegisterError(e))
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<LoginResult, AuthError> {
        return try {
            val response = publicApi.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            val loginResult = response.toDomain()

            if (loginResult == null) {
                AppResult.Failure(AuthError.InvalidServerResponse)
            } else {
                if (loginResult is LoginResult.Success) {
                    saveTokens(loginResult.tokens)
                }

                AppResult.Success(loginResult)
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AuthExceptionMapper.mapLoginError(e))
        }
    }

    override suspend fun forgotPassword(
        email: String
    ): AppResult<ForgotPasswordResult, AuthError> {
        return try {
            val response = publicApi.forgotPassword(
                ForgotPasswordRequest(email = email)
            )

            AppResult.Success(response.toDomain())

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AuthExceptionMapper.mapForgotPasswordError(e))
        }
    }

    override suspend fun resetPassword(
        token: String,
        newPassword: String
    ): AppResult<ResetPasswordResult, AuthError> {
        return try {
            val response = publicApi.resetPassword(
                ResetPasswordRequest(
                    token = token,
                    newPassword = newPassword
                )
            )

            AppResult.Success(response.toDomain())

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AuthExceptionMapper.mapResetPasswordError(e))
        }
    }

    override suspend fun restoreSession(): RestoreSessionResult {
        val oldRefreshToken = backendTokenStorage.getRefreshToken()

        if (oldRefreshToken.isNullOrBlank()) {
            backendTokenStorage.clearTokens()
            Log.i("BACKEND_SESSION", "refresh token is missing")
            return RestoreSessionResult.Unauthenticated
        }

        return when (val result = refreshTokens(oldRefreshToken)) {
            is AppResult.Success -> {
                saveTokens(result.data)

                Log.i("BACKEND_SESSION", "session restored successfully")
                RestoreSessionResult.Authenticated
            }

            is AppResult.Failure -> {
                when (result.error) {
                    AuthError.Unauthorized -> {
                        backendTokenStorage.clearTokens()
                        Log.e("BACKEND_SESSION", "refresh token rejected")
                        RestoreSessionResult.Unauthenticated
                    }

                    AuthError.NetworkUnavailable -> {
                        Log.e("BACKEND_SESSION", "session restore failed: connection error")
                        RestoreSessionResult.NetworkUnavailable
                    }

                    AuthError.ServerUnavailable -> {
                        Log.e("BACKEND_SESSION", "authentication server unavailable")
                        RestoreSessionResult.ServerUnavailable
                    }

                    AuthError.InvalidServerResponse -> {
                        Log.e("BACKEND_SESSION", "refresh response is invalid")
                        RestoreSessionResult.ServerUnavailable
                    }

                    else -> {
                        Log.e("BACKEND_SESSION", "session restore failed")
                        RestoreSessionResult.ServerUnavailable
                    }
                }
            }
        }
    }

    override suspend fun getCurrentUser(): AppResult<AuthUser?, AuthError> {
        return try {
            val response = authenticatedApi.getCurrentUser()

            AppResult.Success(response.user?.toDomain())

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AuthExceptionMapper.mapCurrentUserError(e))
        }
    }

    override suspend fun logoutCurrentSession(): AppResult<Unit, AuthError> {
        val refreshToken = backendTokenStorage.getRefreshToken()

        return try {
            if (!refreshToken.isNullOrBlank()) {
                publicApi.logout(
                    LogoutRequest(refreshToken = refreshToken)
                )

                Log.i("BACKEND_LOGOUT", "backend logout request completed")
            } else {
                Log.i(
                    "BACKEND_LOGOUT",
                    "refreshToken is empty, only local tokens will be cleared"
                )
            }

            AppResult.Success(Unit)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("BACKEND_LOGOUT", "backend logout request failed: ${e.message}", e)
            AppResult.Failure(AuthExceptionMapper.mapLogoutError(e))
        } finally {
            backendTokenStorage.clearTokens()
            Log.i("BACKEND_LOGOUT", "local backend tokens cleared")
        }
    }

    private suspend fun refreshTokens(
        refreshToken: String
    ): AppResult<TokenPair, AuthError> {
        return try {
            val response = publicApi.refresh(
                RefreshRequest(refreshToken = refreshToken)
            )

            val tokenPair = response.toTokenPair()

            if (tokenPair == null) {
                AppResult.Failure(AuthError.InvalidServerResponse)
            } else {
                AppResult.Success(tokenPair)
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AuthExceptionMapper.mapRefreshError(e))
        }
    }

    private fun saveTokens(tokens: TokenPair) {
        backendTokenStorage.saveTokens(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken
        )
    }
}