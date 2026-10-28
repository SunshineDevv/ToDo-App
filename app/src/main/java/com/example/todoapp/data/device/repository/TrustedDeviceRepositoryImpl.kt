
package com.example.todoapp.data.device.repository

import android.os.Build
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.data.device.crypto.DevicePayloadBuilder
import com.example.todoapp.data.device.local.DeviceKeyManager
import com.example.todoapp.data.device.local.TrustedDeviceStorage
import com.example.todoapp.data.device.remote.api.TrustedDeviceApi
import com.example.todoapp.data.device.remote.dto.BindChallengeRequest
import com.example.todoapp.data.device.remote.dto.BindConfirmRequest
import com.example.todoapp.data.device.remote.dto.EnrollmentConfirmRequest
import com.example.todoapp.domain.device.model.TrustedDeviceError
import com.example.todoapp.domain.device.model.TrustedDeviceState
import com.example.todoapp.domain.device.repository.TrustedDeviceRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrustedDeviceRepositoryImpl @Inject constructor(
    private val trustedDeviceApi: TrustedDeviceApi,
    private val deviceKeyManager: DeviceKeyManager,
    private val trustedDeviceStorage: TrustedDeviceStorage
) : TrustedDeviceRepository {

    override suspend fun getStateForUser(userId: String): AppResult<TrustedDeviceState, TrustedDeviceError> {
        if (trustedDeviceStorage.getSessionUserId() != userId) {
            return AppResult.Failure(TrustedDeviceError.CurrentUserUnavailable)
        }

        val deviceId = trustedDeviceStorage.getDeviceId(userId)
        val keyAlias = trustedDeviceStorage.getKeyAlias(userId)

        if (!trustedDeviceStorage.hasEnrollment(userId) || deviceId.isNullOrBlank() || keyAlias.isNullOrBlank()) {
            return AppResult.Success(TrustedDeviceState(enrolled = false, bound = false, deviceId = null))
        }

        if (!deviceKeyManager.hasKey(keyAlias)) {
            return AppResult.Success(TrustedDeviceState(enrolled = false, bound = false, deviceId = null))
        }

        val localState = TrustedDeviceState(
            enrolled = true,
            bound = trustedDeviceStorage.isSessionBound(),
            deviceId = deviceId,
            verified = false
        )

        return try {
            val binding = trustedDeviceApi.getCurrentBinding()
            val isBound = binding.deviceBound && binding.trustedDeviceId == deviceId
            trustedDeviceStorage.setSessionBound(userId, isBound)
            AppResult.Success(TrustedDeviceState(enrolled = true, bound = isBound, deviceId = deviceId, verified = true))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Success(localState)
        }
    }

    override suspend fun enroll(password: String, currentTotp: String?): AppResult<TrustedDeviceState, TrustedDeviceError> {
        return try {
            val challenge = trustedDeviceApi.createEnrollmentChallenge()
            val userId = trustedDeviceStorage.getSessionUserId()

            if (challenge.purpose != "enrollment" || challenge.deviceId != null || userId != challenge.userId) {
                return AppResult.Failure(TrustedDeviceError.InvalidServerResponse)
            }

            val keyAlias = "trusted_device_${challenge.userId}"
            val publicKey = deviceKeyManager.ensureKeyPair(keyAlias)

            val payload = DevicePayloadBuilder.build(
                purpose = challenge.purpose,
                challengeId = challenge.challengeId,
                challenge = challenge.challenge,
                userId = challenge.userId,
                sessionId = challenge.sessionId,
                deviceId = challenge.deviceId
            )

            val signature = deviceKeyManager.signUtf8(alias = keyAlias, payload = payload)
            val manufacturer = Build.MANUFACTURER.trim().ifBlank { "Android" }
            val model = Build.MODEL.trim().ifBlank { "Device" }

            val response = trustedDeviceApi.confirmEnrollment(
                EnrollmentConfirmRequest(
                    challengeId = challenge.challengeId,
                    challenge = challenge.challenge,
                    signature = signature,
                    publicKey = publicKey,
                    label = "$manufacturer $model".take(100),
                    password = password,
                    currentTotp = currentTotp?.trim()?.takeIf { it.isNotBlank() }
                )
            )

            trustedDeviceStorage.saveEnrollment(userId, response.device.id, keyAlias)

            if (trustedDeviceStorage.getSessionUserId() != userId) {
                return AppResult.Failure(TrustedDeviceError.CurrentUserUnavailable)
            }

            AppResult.Success(TrustedDeviceState(enrolled = true, bound = false, deviceId = response.device.id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            AppResult.Failure(
                when (e.code()) {
                    400 -> TrustedDeviceError.InvalidRequest
                    401 -> TrustedDeviceError.InvalidCredentials
                    409 -> TrustedDeviceError.AlreadyRegistered
                    in 500..599 -> TrustedDeviceError.ServerUnavailable
                    else -> TrustedDeviceError.Unknown
                }
            )
        } catch (e: IOException) {
            AppResult.Failure(TrustedDeviceError.NetworkUnavailable)
        } catch (e: Exception) {
            AppResult.Failure(TrustedDeviceError.Unknown)
        }
    }

    override suspend fun bindCurrentSession(): AppResult<TrustedDeviceState, TrustedDeviceError> {
        val userId = trustedDeviceStorage.getSessionUserId()
            ?: return AppResult.Failure(TrustedDeviceError.CurrentUserUnavailable)
        val deviceId = trustedDeviceStorage.getDeviceId(userId)
            ?: return AppResult.Failure(TrustedDeviceError.NotRegistered)
        val keyAlias = trustedDeviceStorage.getKeyAlias(userId)
            ?: return AppResult.Failure(TrustedDeviceError.NotRegistered)

        if (!deviceKeyManager.hasKey(keyAlias)) {
            return AppResult.Failure(TrustedDeviceError.PrivateKeyMissing)
        }

        return try {
            val challenge = trustedDeviceApi.createBindChallenge(BindChallengeRequest(deviceId = deviceId))

            if (challenge.purpose != "bind" || challenge.userId != userId || challenge.deviceId != deviceId) {
                return AppResult.Failure(TrustedDeviceError.InvalidServerResponse)
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

            trustedDeviceApi.confirmBind(
                BindConfirmRequest(
                    deviceId = deviceId,
                    challengeId = challenge.challengeId,
                    challenge = challenge.challenge,
                    signature = signature
                )
            )

            val binding = trustedDeviceApi.getCurrentBinding()

            if (!binding.deviceBound || binding.trustedDeviceId != deviceId) {
                return AppResult.Failure(TrustedDeviceError.InvalidServerResponse)
            }

            if (trustedDeviceStorage.getSessionUserId() != userId) {
                return AppResult.Failure(TrustedDeviceError.CurrentUserUnavailable)
            }

            trustedDeviceStorage.setSessionBound(userId, true)
            AppResult.Success(TrustedDeviceState(enrolled = true, bound = true, deviceId = deviceId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            AppResult.Failure(
                when (e.code()) {
                    400 -> TrustedDeviceError.InvalidRequest
                    401, 403 -> TrustedDeviceError.Unauthorized
                    in 500..599 -> TrustedDeviceError.ServerUnavailable
                    else -> TrustedDeviceError.Unknown
                }
            )
        } catch (e: IOException) {
            AppResult.Failure(TrustedDeviceError.NetworkUnavailable)
        } catch (e: Exception) {
            AppResult.Failure(TrustedDeviceError.Unknown)
        }
    }

    override suspend fun revokeCurrentDevice(): AppResult<Unit, TrustedDeviceError> {
        val userId = trustedDeviceStorage.getSessionUserId()
            ?: return AppResult.Failure(TrustedDeviceError.CurrentUserUnavailable)
        val deviceId = trustedDeviceStorage.getDeviceId(userId)
            ?: return AppResult.Failure(TrustedDeviceError.NotRegistered)
        val keyAlias = trustedDeviceStorage.getKeyAlias(userId)

        return try {
            trustedDeviceApi.revokeDevice(deviceId)

            if (!keyAlias.isNullOrBlank() && deviceKeyManager.hasKey(keyAlias)) {
                deviceKeyManager.deleteKey(keyAlias)
            }

            trustedDeviceStorage.clearEnrollment(userId)
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            AppResult.Failure(
                when (e.code()) {
                    401, 403 -> TrustedDeviceError.Unauthorized
                    in 500..599 -> TrustedDeviceError.ServerUnavailable
                    else -> TrustedDeviceError.Unknown
                }
            )
        } catch (e: IOException) {
            AppResult.Failure(TrustedDeviceError.NetworkUnavailable)
        } catch (e: Exception) {
            AppResult.Failure(TrustedDeviceError.Unknown)
        }
    }
}
