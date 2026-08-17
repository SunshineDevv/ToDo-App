package com.example.todoapp.database.repository.auth

import android.util.Log
import com.example.todoapp.database.local.auth.BackendTokenStorage
import com.example.todoapp.network.api.AuthBackendApi
import com.example.todoapp.network.dto.BackendUserResponse
import com.example.todoapp.network.dto.LoginRequest
import com.example.todoapp.network.dto.LoginResponse
import com.example.todoapp.network.dto.LogoutRequest
import com.example.todoapp.network.dto.RefreshRequest
import com.example.todoapp.network.dto.RegisterRequest
import com.example.todoapp.network.dto.RegisterResponse
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthBackendApi,
    private val backendTokenStorage: BackendTokenStorage
) {

    suspend fun register(email: String, password: String, name: String): RegisterResponse {
        return api.register(
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
        return api.login(
            LoginRequest(
                email = email,
                password = password
            )
        )
    }

    suspend fun refresh(
        refreshToken: String
    ): LoginResponse {
        return api.refresh(
            RefreshRequest(
                refreshToken = refreshToken
            )
        )
    }

    suspend fun getCurrentUser(): BackendUserResponse? {
        return api.getCurrentUser().user
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

    suspend fun logoutCurrentSession() {
        val refreshToken = backendTokenStorage.getRefreshToken()

        try {
            if (!refreshToken.isNullOrBlank()) {
                api.logout(
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

    suspend fun checkHealth() = api.health()
}