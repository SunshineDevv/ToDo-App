package com.example.todoapp.network.api

import com.example.todoapp.network.dto.CurrentUserResponse
import com.example.todoapp.network.dto.ForgotPasswordRequest
import com.example.todoapp.network.dto.ForgotPasswordResponse
import com.example.todoapp.network.dto.HealthResponse
import com.example.todoapp.network.dto.LoginRequest
import com.example.todoapp.network.dto.LoginResponse
import com.example.todoapp.network.dto.LogoutRequest
import com.example.todoapp.network.dto.RefreshRequest
import com.example.todoapp.network.dto.RefreshResponse
import com.example.todoapp.network.dto.RegisterRequest
import com.example.todoapp.network.dto.RegisterResponse
import com.example.todoapp.network.dto.ResetPasswordRequest
import com.example.todoapp.network.dto.ResetPasswordResponse
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