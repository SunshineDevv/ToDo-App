package com.example.todoapp.data.auth.mapper

import com.example.todoapp.domain.auth.model.AuthError
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

object AuthExceptionMapper {

    fun mapLoginError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable
            is HttpException -> {
                when (throwable.code()) {
                    400, 401 -> AuthError.InvalidCredentials
                    404 -> AuthError.EndpointNotFound
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("Login failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapRegisterError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable
            is HttpException -> {
                when (throwable.code()) {
                    400 -> AuthError.InvalidEmail
                    409 -> AuthError.EmailAlreadyExists
                    404 -> AuthError.EndpointNotFound
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("Registration failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapForgotPasswordError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable
            is HttpException -> {
                when (throwable.code()) {
                    400 -> AuthError.InvalidEmail
                    404 -> AuthError.EndpointNotFound
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("Password reset request failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapResetPasswordError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable
            is HttpException -> {
                when (throwable.code()) {
                    400, 401, 403 -> AuthError.InvalidOrExpiredResetToken
                    404 -> AuthError.EndpointNotFound
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("Password reset failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapCurrentUserError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable
            is HttpException -> {
                when (throwable.code()) {
                    401, 403 -> AuthError.Unauthorized
                    404 -> AuthError.EndpointNotFound
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("Current user request failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapLogoutError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable
            is HttpException -> {
                when (throwable.code()) {
                    401, 403 -> AuthError.Unauthorized
                    404 -> AuthError.EndpointNotFound
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("Logout failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapRefreshError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable
            is HttpException -> {
                when (throwable.code()) {
                    401, 403 -> AuthError.Unauthorized
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("Refresh failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapMfaLoginError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable

            is HttpException -> {
                when (throwable.code()) {
                    400 -> {
                        when (extractBackendErrorCode(throwable)) {
                            "MFA_CODE_REPLAYED" -> AuthError.MfaCodeAlreadyUsed
                            "MFA_CODE_INVALID" -> AuthError.InvalidMfaCode
                            "MFA_NOT_CONFIGURED",
                            "MFA_NOT_ENABLED" -> AuthError.MfaNotConfigured

                            else -> AuthError.InvalidMfaCode
                        }
                    }

                    401, 403 -> AuthError.InvalidMfaChallenge
                    404 -> AuthError.EndpointNotFound
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("MFA login failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapMfaSetupError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable

            is HttpException -> {
                when (throwable.code()) {
                    400 -> {
                        when (extractBackendErrorCode(throwable)) {
                            "INVALID_CREDENTIALS" -> AuthError.InvalidPassword

                            "MFA_CODE_REPLAYED" -> AuthError.MfaCodeAlreadyUsed
                            "MFA_CODE_INVALID",
                            "MFA_CURRENT_CODE_REQUIRED" -> AuthError.InvalidMfaCode

                            "MFA_NOT_CONFIGURED",
                            "MFA_NOT_ENABLED" -> AuthError.MfaNotConfigured

                            "VALIDATION_ERROR" -> AuthError.InvalidServerResponse

                            else -> AuthError.Unknown("MFA setup failed: HTTP 400")
                        }
                    }

                    401, 403 -> AuthError.Unauthorized
                    404 -> AuthError.EndpointNotFound
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("MFA setup failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapMfaConfirmError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable

            is HttpException -> {
                when (throwable.code()) {
                    400 -> {
                        when (extractBackendErrorCode(throwable)) {
                            "MFA_CODE_REPLAYED" -> AuthError.MfaCodeAlreadyUsed
                            "MFA_CODE_INVALID" -> AuthError.InvalidMfaCode

                            "MFA_ENROLLMENT_NOT_FOUND",
                            "MFA_ENROLLMENT_EXPIRED" -> AuthError.MfaSetupExpired

                            else -> AuthError.InvalidMfaCode
                        }
                    }

                    401, 403 -> AuthError.Unauthorized
                    404 -> AuthError.MfaSetupExpired
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("MFA confirmation failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    fun mapMfaDisableError(throwable: Throwable): AuthError {
        return when (throwable) {
            is IOException -> AuthError.NetworkUnavailable

            is HttpException -> {
                when (throwable.code()) {
                    400 -> {
                        when (extractBackendErrorCode(throwable)) {
                            "INVALID_CREDENTIALS" -> AuthError.InvalidPassword

                            "MFA_CODE_REPLAYED" -> AuthError.MfaCodeAlreadyUsed
                            "MFA_CODE_INVALID" -> AuthError.InvalidMfaCode

                            "MFA_NOT_CONFIGURED",
                            "MFA_NOT_ENABLED" -> AuthError.MfaNotConfigured

                            else -> AuthError.InvalidMfaCode
                        }
                    }

                    401, 403 -> AuthError.Unauthorized
                    404 -> AuthError.MfaNotConfigured
                    429 -> AuthError.TooManyRequests
                    in 500..599 -> AuthError.ServerUnavailable
                    else -> AuthError.Unknown("MFA disable failed: HTTP ${throwable.code()}")
                }
            }

            else -> AuthError.Unknown(throwable.message)
        }
    }

    private fun extractBackendErrorCode(exception: HttpException): String? {
        val rawBody = exception.response()?.errorBody()?.string() ?: return null

        return runCatching {
            JSONObject(rawBody)
                .optJSONObject("error")
                ?.optString("code")
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}