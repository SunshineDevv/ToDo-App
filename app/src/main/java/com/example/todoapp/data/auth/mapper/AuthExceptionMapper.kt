package com.example.todoapp.data.auth.mapper

import com.example.todoapp.domain.auth.model.AuthError
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
}