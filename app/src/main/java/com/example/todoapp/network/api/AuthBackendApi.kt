package com.example.todoapp.network.api

import com.example.todoapp.network.dto.HealthResponse
import com.example.todoapp.network.dto.LoginRequest
import com.example.todoapp.network.dto.LoginResponse
import com.example.todoapp.network.dto.LogoutRequest
import com.example.todoapp.network.dto.RefreshRequest
import com.example.todoapp.network.dto.RegisterRequest
import com.example.todoapp.network.dto.RegisterResponse
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
    suspend fun refresh(@Body request: RefreshRequest): LoginResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest)
}