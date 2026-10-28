
package com.example.todoapp.data.auth.remote.refresh

import com.example.todoapp.data.auth.remote.api.AuthBackendApi
import com.example.todoapp.data.auth.remote.dto.DeviceProofRequest
import com.example.todoapp.data.auth.remote.dto.RefreshChallengeRequest
import com.example.todoapp.data.auth.remote.dto.RefreshRequest
import com.example.todoapp.data.auth.remote.dto.RefreshResponse
import com.example.todoapp.data.device.crypto.DevicePayloadBuilder
import com.example.todoapp.data.device.local.DeviceKeyManager
import com.example.todoapp.data.device.local.TrustedDeviceStorage
import com.example.todoapp.di.PublicBackendApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceAwareRefreshManager @Inject constructor(
    @PublicBackendApi private val publicApi: AuthBackendApi,
    private val trustedDeviceStorage: TrustedDeviceStorage,
    private val deviceKeyManager: DeviceKeyManager
) {
    suspend fun refresh(refreshToken: String): RefreshResponse {
        val userId = trustedDeviceStorage.getSessionUserId()
            ?: throw DeviceBoundRefreshException("Session user is missing")

        if (!trustedDeviceStorage.isSessionBound()) {
            return publicApi.refresh(RefreshRequest(refreshToken = refreshToken))
        }

        val deviceId = trustedDeviceStorage.getDeviceId(userId)
            ?: throw DeviceBoundRefreshException("Bound session has no deviceId")
        val keyAlias = trustedDeviceStorage.getKeyAlias(userId)
            ?: throw DeviceBoundRefreshException("Bound session has no key alias")

        if (!deviceKeyManager.hasKey(keyAlias)) {
            throw DeviceBoundRefreshException("Trusted device private key is missing")
        }

        val challenge = publicApi.createRefreshChallenge(RefreshChallengeRequest(refreshToken = refreshToken))

        if (challenge.purpose != "refresh" || challenge.userId != userId || challenge.deviceId != deviceId) {
            throw DeviceBoundRefreshException("Refresh challenge does not match the current session")
        }

        val payload = DevicePayloadBuilder.build(
            purpose = challenge.purpose,
            challengeId = challenge.challengeId,
            challenge = challenge.challenge,
            userId = challenge.userId,
            sessionId = challenge.sessionId,
            deviceId = challenge.deviceId
        )

        val signature = deviceKeyManager.signUtf8(alias = keyAlias, payload = payload)

        return publicApi.refresh(
            RefreshRequest(
                refreshToken = refreshToken,
                deviceProof = DeviceProofRequest(
                    challengeId = challenge.challengeId,
                    challenge = challenge.challenge,
                    signature = signature
                )
            )
        )
    }
}

class DeviceBoundRefreshException(message: String) : IllegalStateException(message)
