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
    @PublicBackendApi
    private val publicApi: AuthBackendApi,
    private val trustedDeviceStorage: TrustedDeviceStorage,
    private val deviceKeyManager: DeviceKeyManager
) {

    suspend fun refresh(
        refreshToken: String
    ): RefreshResponse {

        /*
         * Legacy / unbound session:
         * сохраняем полностью старое поведение.
         */
        if (!trustedDeviceStorage.isDeviceBound()) {
            return publicApi.refresh(
                RefreshRequest(
                    refreshToken = refreshToken
                )
            )
        }

        /*
         * Если локально session считается bound,
         * НИКОГДА не откатываемся на обычный refresh.
         */
        val userId = trustedDeviceStorage.getUserId()
            ?: throw DeviceBoundRefreshException(
                "Bound session has no stored userId"
            )

        val deviceId = trustedDeviceStorage.getDeviceId()
            ?: throw DeviceBoundRefreshException(
                "Bound session has no stored deviceId"
            )

        val keyAlias = trustedDeviceStorage.getKeyAlias()
            ?: throw DeviceBoundRefreshException(
                "Bound session has no stored key alias"
            )

        if (!deviceKeyManager.hasKey(keyAlias)) {
            throw DeviceBoundRefreshException(
                "Trusted device private key is missing"
            )
        }

        /*
         * 1. Получаем одноразовый refresh challenge.
         */
        val challenge = publicApi.createRefreshChallenge(
            RefreshChallengeRequest(
                refreshToken = refreshToken
            )
        )

        /*
         * Не подписываем неожиданный серверный контекст.
         */
        if (challenge.purpose != "refresh") {
            throw DeviceBoundRefreshException(
                "Unexpected challenge purpose: ${challenge.purpose}"
            )
        }

        if (challenge.userId != userId) {
            throw DeviceBoundRefreshException(
                "Refresh challenge user mismatch"
            )
        }

        if (challenge.deviceId != deviceId) {
            throw DeviceBoundRefreshException(
                "Refresh challenge device mismatch"
            )
        }

        /*
         * 2. Строим тот же canonical payload,
         * который проверяет backend.
         */
        val payload = DevicePayloadBuilder.build(
            purpose = challenge.purpose,
            challengeId = challenge.challengeId,
            challenge = challenge.challenge,
            userId = challenge.userId,
            sessionId = challenge.sessionId,
            deviceId = challenge.deviceId
        )

        /*
         * 3. Подписываем приватным ключом Android Keystore.
         */
        val signature = deviceKeyManager.signUtf8(
            alias = keyAlias,
            payload = payload
        )

        /*
         * 4. Отправляем refresh token + ECDSA proof.
         */
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

class DeviceBoundRefreshException(
    message: String
) : IllegalStateException(message)