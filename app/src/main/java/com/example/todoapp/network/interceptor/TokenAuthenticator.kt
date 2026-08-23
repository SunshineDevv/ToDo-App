package com.example.todoapp.network.interceptor

import android.util.Log
import com.example.todoapp.database.local.auth.BackendTokenStorage
import com.example.todoapp.di.PublicBackendApi
import com.example.todoapp.network.api.AuthBackendApi
import com.example.todoapp.network.dto.RefreshRequest
import com.example.todoapp.session.AuthSessionEventManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    @PublicBackendApi private val publicApi: AuthBackendApi,
    private val backendTokenStorage: BackendTokenStorage,
    private val authSessionEventManager: AuthSessionEventManager
) : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {
        if (responseCount(response) >= MAX_AUTH_RETRY_COUNT) {
            Log.e("BACKEND_TOKEN", "authentication retry limit reached")
            return null
        }

        val requestAccessToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        synchronized(this) {
            val latestAccessToken = backendTokenStorage.getAccessToken()

            if (
                !latestAccessToken.isNullOrBlank() &&
                !requestAccessToken.isNullOrBlank() &&
                latestAccessToken != requestAccessToken
            ) {
                Log.i("BACKEND_TOKEN", "access token was already refreshed, retrying request")

                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccessToken")
                    .build()
            }

            val refreshToken = backendTokenStorage.getRefreshToken()

            if (refreshToken.isNullOrBlank()) {
                expireLocalSession("refresh token is missing")
                return null
            }

            return try {
                val refreshResponse = runBlocking {
                    publicApi.refresh(
                        RefreshRequest(refreshToken = refreshToken)
                    )
                }

                val newAccessToken = refreshResponse.accessToken
                val newRefreshToken = refreshResponse.refreshToken

                if (newAccessToken.isNullOrBlank() || newRefreshToken.isNullOrBlank()) {
                    Log.e("BACKEND_TOKEN", "refresh response does not contain tokens")
                    return null
                }

                backendTokenStorage.saveTokens(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken
                )

                authSessionEventManager.markAuthenticated()

                Log.i("BACKEND_TOKEN", "tokens refreshed after 401")

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()

            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    expireLocalSession("refresh token rejected: HTTP ${e.code()}")
                } else {
                    Log.e("BACKEND_TOKEN", "refresh failed: HTTP ${e.code()}", e)
                }

                null

            } catch (e: IOException) {
                Log.e("BACKEND_TOKEN", "refresh failed: connection error", e)
                null

            } catch (e: Exception) {
                Log.e("BACKEND_TOKEN", "unexpected refresh failure: ${e.message}", e)
                null
            }
        }
    }

    private fun expireLocalSession(reason: String) {
        backendTokenStorage.clearTokens()
        authSessionEventManager.notifySessionExpired()
        Log.e("BACKEND_SESSION", "session expired: $reason")
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse

        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }

        return result
    }

    private companion object {
        const val MAX_AUTH_RETRY_COUNT = 2
    }
}