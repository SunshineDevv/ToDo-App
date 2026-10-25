package com.example.todoapp.data.device.remote.api

import com.example.todoapp.data.device.remote.dto.BindChallengeRequest
import com.example.todoapp.data.device.remote.dto.BindConfirmRequest
import com.example.todoapp.data.device.remote.dto.CurrentBindingResponse
import com.example.todoapp.data.device.remote.dto.DeviceChallengeResponse
import com.example.todoapp.data.device.remote.dto.EnrollmentConfirmRequest
import com.example.todoapp.data.device.remote.dto.EnrollmentConfirmResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface TrustedDeviceApi {

    @POST("api/devices/enrollment/challenge")
    suspend fun createEnrollmentChallenge(): DeviceChallengeResponse

    @POST("api/devices/enrollment/confirm")
    suspend fun confirmEnrollment(
        @Body request: EnrollmentConfirmRequest
    ): EnrollmentConfirmResponse

    @POST("api/devices/bind/challenge")
    suspend fun createBindChallenge(
        @Body request: BindChallengeRequest
    ): DeviceChallengeResponse

    @POST("api/devices/bind/confirm")
    suspend fun confirmBind(
        @Body request: BindConfirmRequest
    )

    @GET("api/devices/current-binding")
    suspend fun getCurrentBinding(): CurrentBindingResponse

    @DELETE("api/devices/{deviceId}")
    suspend fun revokeDevice(
        @Path("deviceId") deviceId: String
    )
}