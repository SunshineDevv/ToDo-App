package com.example.todoapp.presentation.trusteddevice

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.device.crypto.DevicePayloadBuilder
import com.example.todoapp.data.device.local.DeviceKeyManager
import com.example.todoapp.data.device.local.TrustedDeviceStorage
import com.example.todoapp.data.device.remote.api.TrustedDeviceApi
import com.example.todoapp.data.device.remote.dto.BindChallengeRequest
import com.example.todoapp.data.device.remote.dto.BindConfirmRequest
import com.example.todoapp.data.device.remote.dto.EnrollmentConfirmRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.todoapp.domain.auth.usecase.LogoutUseCase
import retrofit2.HttpException
import javax.inject.Inject

data class TrustedDeviceUiState(
    val loading: Boolean = false,
    val enrolled: Boolean = false,
    val bound: Boolean = false,
    val deviceId: String? = null,
    val message: String? = null,
    val isError: Boolean = false,
    val requireLogin: Boolean = false
)

@HiltViewModel
class TrustedDeviceViewModel @Inject constructor(
    private val trustedDeviceApi: TrustedDeviceApi,
    private val deviceKeyManager: DeviceKeyManager,
    private val trustedDeviceStorage: TrustedDeviceStorage,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state =
        MutableStateFlow(TrustedDeviceUiState())

    val state = _state.asStateFlow()

    fun loadState() {
        viewModelScope.launch {

            val deviceId =
                trustedDeviceStorage.getDeviceId()

            val keyAlias =
                trustedDeviceStorage.getKeyAlias()

            val hasEnrollment =
                trustedDeviceStorage.hasEnrollment()

            if (
                !hasEnrollment ||
                deviceId.isNullOrBlank() ||
                keyAlias.isNullOrBlank() ||
                !deviceKeyManager.hasKey(keyAlias)
            ) {
                _state.value =
                    TrustedDeviceUiState(
                        enrolled = false,
                        bound = false
                    )

                return@launch
            }

            _state.value =
                TrustedDeviceUiState(
                    loading = true,
                    enrolled = true,
                    bound = trustedDeviceStorage.isDeviceBound(),
                    deviceId = deviceId
                )

            try {
                val binding =
                    trustedDeviceApi.getCurrentBinding()

                val isBound =
                    binding.deviceBound &&
                            binding.trustedDeviceId == deviceId

                if (isBound) {
                    trustedDeviceStorage.markBound()
                } else {
                    trustedDeviceStorage.markUnbound()
                }

                _state.value =
                    TrustedDeviceUiState(
                        enrolled = true,
                        bound = isBound,
                        deviceId = deviceId
                    )

            } catch (e: HttpException) {

                _state.value =
                    TrustedDeviceUiState(
                        enrolled = true,
                        bound = trustedDeviceStorage.isDeviceBound(),
                        deviceId = deviceId,
                        message = "Unable to verify current session.",
                        isError = true
                    )

            } catch (e: Exception) {

                _state.value =
                    TrustedDeviceUiState(
                        enrolled = true,
                        bound = trustedDeviceStorage.isDeviceBound(),
                        deviceId = deviceId,
                        message = e.message
                            ?: "Unable to load trusted device status.",
                        isError = true
                    )
            }
        }
    }

    fun enroll(
        password: String,
        currentTotp: String?
    ) {
        if (password.isBlank()) {
            showError(
                "Password cannot be empty."
            )

            return
        }

        _state.value =
            _state.value.copy(
                loading = true,
                message = null,
                isError = false
            )

        viewModelScope.launch {
            try {
                val challenge =
                    trustedDeviceApi
                        .createEnrollmentChallenge()

                require(
                    challenge.purpose == "enrollment"
                ) {
                    "Unexpected challenge purpose."
                }

                require(
                    challenge.deviceId == null
                ) {
                    "Enrollment challenge contains unexpected deviceId."
                }

                val keyAlias =
                    "trusted_device_${challenge.userId}"

                val publicKey =
                    deviceKeyManager.ensureKeyPair(
                        keyAlias
                    )

                val payload =
                    DevicePayloadBuilder.build(
                        purpose = challenge.purpose,
                        challengeId = challenge.challengeId,
                        challenge = challenge.challenge,
                        userId = challenge.userId,
                        sessionId = challenge.sessionId,
                        deviceId = challenge.deviceId
                    )

                val signature =
                    deviceKeyManager.signUtf8(
                        alias = keyAlias,
                        payload = payload
                    )

                val manufacturer =
                    Build.MANUFACTURER
                        .trim()
                        .ifBlank {
                            "Android"
                        }

                val model =
                    Build.MODEL
                        .trim()
                        .ifBlank {
                            "Device"
                        }

                val deviceLabel =
                    "$manufacturer $model"
                        .take(100)

                val response =
                    trustedDeviceApi
                        .confirmEnrollment(
                            EnrollmentConfirmRequest(
                                challengeId =
                                    challenge.challengeId,
                                challenge =
                                    challenge.challenge,
                                signature =
                                    signature,
                                publicKey =
                                    publicKey,
                                label =
                                    deviceLabel,
                                password =
                                    password,
                                currentTotp =
                                    currentTotp
                                        ?.trim()
                                        ?.takeIf {
                                            it.isNotBlank()
                                        }
                            )
                        )

                trustedDeviceStorage
                    .saveEnrollment(
                        userId =
                            challenge.userId,
                        deviceId =
                            response.device.id,
                        keyAlias =
                            keyAlias
                    )

                _state.value =
                    TrustedDeviceUiState(
                        enrolled = true,
                        bound = false,
                        deviceId = response.device.id,
                        message = "This device was registered successfully.",
                        isError = false
                    )

            } catch (e: HttpException) {

                _state.value =
                    _state.value.copy(
                        loading = false,
                        message =
                            when (e.code()) {
                                400 ->
                                    "Invalid registration data."

                                401 ->
                                    "Password or MFA code is incorrect."

                                409 ->
                                    "This key is already registered."

                                else ->
                                    "Registration failed. HTTP ${e.code()}."
                            },
                        isError = true
                    )

            } catch (e: Exception) {

                _state.value =
                    _state.value.copy(
                        loading = false,
                        message =
                            e.message
                                ?: "Registration failed.",
                        isError = true
                    )
            }
        }
    }

    fun bindCurrentSession() {

        _state.value =
            _state.value.copy(
                loading = true,
                message = null,
                isError = false
            )

        viewModelScope.launch {
            try {
                val storedUserId =
                    trustedDeviceStorage
                        .getUserId()
                        ?: throw IllegalStateException(
                            "Trusted device userId is missing."
                        )

                val deviceId =
                    trustedDeviceStorage
                        .getDeviceId()
                        ?: throw IllegalStateException(
                            "Trusted deviceId is missing."
                        )

                val keyAlias =
                    trustedDeviceStorage
                        .getKeyAlias()
                        ?: throw IllegalStateException(
                            "Trusted device key is missing."
                        )

                require(
                    deviceKeyManager.hasKey(
                        keyAlias
                    )
                ) {
                    "Trusted device private key is missing."
                }

                val challenge =
                    trustedDeviceApi
                        .createBindChallenge(
                            BindChallengeRequest(
                                deviceId =
                                    deviceId
                            )
                        )

                require(
                    challenge.purpose == "bind"
                ) {
                    "Unexpected challenge purpose."
                }

                require(
                    challenge.userId == storedUserId
                ) {
                    "Challenge user mismatch."
                }

                require(
                    challenge.deviceId == deviceId
                ) {
                    "Challenge device mismatch."
                }

                val payload =
                    DevicePayloadBuilder.build(
                        purpose =
                            challenge.purpose,
                        challengeId =
                            challenge.challengeId,
                        challenge =
                            challenge.challenge,
                        userId =
                            challenge.userId,
                        sessionId =
                            challenge.sessionId,
                        deviceId =
                            challenge.deviceId
                    )

                val signature =
                    deviceKeyManager.signUtf8(
                        alias =
                            keyAlias,
                        payload =
                            payload
                    )

                trustedDeviceApi
                    .confirmBind(
                        BindConfirmRequest(
                            deviceId =
                                deviceId,
                            challengeId =
                                challenge.challengeId,
                            challenge =
                                challenge.challenge,
                            signature =
                                signature
                        )
                    )

                val binding =
                    trustedDeviceApi
                        .getCurrentBinding()

                require(
                    binding.deviceBound
                ) {
                    "Backend reports session as unbound."
                }

                require(
                    binding.trustedDeviceId == deviceId
                ) {
                    "Unexpected trustedDeviceId."
                }

                trustedDeviceStorage
                    .markBound()

                _state.value =
                    TrustedDeviceUiState(
                        enrolled = true,
                        bound = true,
                        deviceId = deviceId,
                        message = "Current session is now protected by this device.",
                        isError = false
                    )

            } catch (e: HttpException) {

                _state.value =
                    _state.value.copy(
                        loading = false,
                        message =
                            "Unable to bind current session. HTTP ${e.code()}.",
                        isError = true
                    )

            } catch (e: Exception) {

                _state.value =
                    _state.value.copy(
                        loading = false,
                        message =
                            e.message
                                ?: "Unable to bind current session.",
                        isError = true
                    )
            }
        }
    }

    fun revokeTrustedDevice() {

        val deviceId =
            trustedDeviceStorage
                .getDeviceId()

        val keyAlias =
            trustedDeviceStorage
                .getKeyAlias()

        if (
            deviceId.isNullOrBlank()
        ) {
            showError(
                "Trusted device is not registered."
            )

            return
        }

        _state.value =
            _state.value.copy(
                loading = true,
                message = null,
                isError = false
            )

        viewModelScope.launch {
            try {
                trustedDeviceApi.revokeDevice(deviceId)

                if (!keyAlias.isNullOrBlank() && deviceKeyManager.hasKey(keyAlias)) {
                    deviceKeyManager.deleteKey(keyAlias)
                }

                trustedDeviceStorage.clear()

                logoutUseCase()

                _state.value =
                    TrustedDeviceUiState(
                        enrolled = false,
                        bound = false,
                        deviceId = null,
                        message = "Trusted device was revoked.",
                        isError = false,
                        requireLogin = true
                    )

            } catch (e: HttpException) {

                _state.value =
                    _state.value.copy(
                        loading = false,
                        message =
                            "Unable to revoke trusted device. HTTP ${e.code()}.",
                        isError = true
                    )

            } catch (e: Exception) {

                _state.value =
                    _state.value.copy(
                        loading = false,
                        message =
                            e.message
                                ?: "Unable to revoke trusted device.",
                        isError = true
                    )
            }
        }
    }

    fun clearMessage() {
        _state.value =
            _state.value.copy(
                message = null,
                isError = false
            )
    }

    private fun showError(
        message: String
    ) {
        _state.value =
            _state.value.copy(
                loading = false,
                message = message,
                isError = true
            )
    }
}