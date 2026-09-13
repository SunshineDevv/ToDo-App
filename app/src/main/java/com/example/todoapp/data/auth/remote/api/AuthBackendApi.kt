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
import com.example.todoapp.data.auth.remote.dto.VerifyMfaLoginRequest
import com.example.todoapp.data.auth.remote.dto.VerifyMfaLoginResponse
import com.example.todoapp.data.auth.remote.dto.MfaDisableRequest
import com.example.todoapp.data.auth.remote.dto.MfaSetupBeginRequest
import com.example.todoapp.data.auth.remote.dto.MfaSetupBeginResponse
import com.example.todoapp.data.auth.remote.dto.MfaSetupConfirmRequest
import com.example.todoapp.data.auth.remote.dto.MfaStatusResponse
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

    @POST("api/auth/mfa/verify")
    suspend fun verifyMfaLogin(@Body request: VerifyMfaLoginRequest): VerifyMfaLoginResponse

    @POST("api/mfa/setup/begin")
    suspend fun beginMfaSetup(@Body request: MfaSetupBeginRequest): MfaSetupBeginResponse

    @POST("api/mfa/setup/confirm")
    suspend fun confirmMfaSetup(@Body request: MfaSetupConfirmRequest): MfaStatusResponse

    @POST("api/mfa/disable")
    suspend fun disableMfa(@Body request: MfaDisableRequest): MfaStatusResponse

    @GET("api/auth/me")
    suspend fun getCurrentUser(): CurrentUserResponse
}