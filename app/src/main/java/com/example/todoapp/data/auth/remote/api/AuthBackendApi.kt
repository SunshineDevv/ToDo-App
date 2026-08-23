package com.example.todoapp.data.auth.remote.api

import com.example.todoapp.data.auth.remote.dto.CurrentUserResponse
import com.example.todoapp.data.auth.remote.dto.ForgotPasswordRequest
import com.example.todoapp.data.auth.remote.dto.ForgotPasswordResponse
import com.example.todoapp.data.auth.remote.dto.HealthResponse
import com.example.todoapp.data.auth.remote.dto.LoginRequest
import com.example.todoapp.data.auth.remote.dto.LoginResponse
import com.example.todoapp.data.auth.remote.dto.LogoutRequest
import com.example.todoapp.data.auth.remote.dto.RefreshRequest
import com.example.todoapp.data.auth.remote.dto.RefreshResponse
import com.example.todoapp.data.auth.remote.dto.RegisterRequest
import com.example.todoapp.data.auth.remote.dto.RegisterResponse
import com.example.todoapp.data.auth.remote.dto.ResetPasswordRequest
import com.example.todoapp.data.auth.remote.dto.ResetPasswordResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthBackendApi {

    @GET("health")
    suspend fun health(): HealthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @POST("api/auth/password/forgot")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("api/auth/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ResetPasswordResponse

    @GET("api/auth/me")
    suspend fun getCurrentUser(): CurrentUserResponse
}