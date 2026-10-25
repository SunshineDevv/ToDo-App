package com.example.todoapp.presentation.trusteddevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.domain.device.model.TrustedDeviceError
import com.example.todoapp.domain.device.usecase.BindTrustedDeviceUseCase
import com.example.todoapp.domain.device.usecase.EnrollTrustedDeviceUseCase
import com.example.todoapp.domain.device.usecase.GetTrustedDeviceStateUseCase
import com.example.todoapp.domain.device.usecase.RevokeTrustedDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val getTrustedDeviceStateUseCase:
    GetTrustedDeviceStateUseCase,
    private val enrollTrustedDeviceUseCase:
    EnrollTrustedDeviceUseCase,
    private val bindTrustedDeviceUseCase:
    BindTrustedDeviceUseCase,
    private val revokeTrustedDeviceUseCase:
    RevokeTrustedDeviceUseCase
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            TrustedDeviceUiState()
        )

    val state =
        _state.asStateFlow()

    fun loadState() {

        _state.value =
            _state.value.copy(
                loading = true,
                message = null,
                isError = false
            )

        viewModelScope.launch {

            when (
                val result =
                    getTrustedDeviceStateUseCase()
            ) {

                is AppResult.Success -> {

                    val device =
                        result.data

                    _state.value =
                        TrustedDeviceUiState(
                            loading = false,
                            enrolled =
                                device.enrolled,
                            bound =
                                device.bound,
                            deviceId =
                                device.deviceId,
                            message =
                                if (
                                    device.enrolled &&
                                    !device.verified
                                ) {
                                    "Unable to verify current session."
                                } else {
                                    null
                                },
                            isError =
                                device.enrolled &&
                                        !device.verified
                        )
                }

                is AppResult.Failure -> {

                    _state.value =
                        TrustedDeviceUiState(
                            loading = false,
                            enrolled = false,
                            bound = false,
                            message =
                                errorMessage(
                                    result.error
                                ),
                            isError = true
                        )
                }
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

            when (
                val result =
                    enrollTrustedDeviceUseCase(
                        password = password,
                        currentTotp = currentTotp
                    )
            ) {

                is AppResult.Success -> {

                    val device =
                        result.data

                    _state.value =
                        TrustedDeviceUiState(
                            enrolled =
                                device.enrolled,
                            bound =
                                device.bound,
                            deviceId =
                                device.deviceId,
                            message =
                                "This device was registered successfully.",
                            isError = false
                        )
                }

                is AppResult.Failure -> {

                    _state.value =
                        _state.value.copy(
                            loading = false,
                            message =
                                errorMessage(
                                    result.error
                                ),
                            isError = true
                        )
                }
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

            when (
                val result =
                    bindTrustedDeviceUseCase()
            ) {

                is AppResult.Success -> {

                    val device =
                        result.data

                    _state.value =
                        TrustedDeviceUiState(
                            enrolled =
                                device.enrolled,
                            bound =
                                device.bound,
                            deviceId =
                                device.deviceId,
                            message =
                                "Current session is now protected by this device.",
                            isError = false
                        )
                }

                is AppResult.Failure -> {

                    _state.value =
                        _state.value.copy(
                            loading = false,
                            message =
                                errorMessage(
                                    result.error
                                ),
                            isError = true
                        )
                }
            }
        }
    }

    fun revokeTrustedDevice() {

        _state.value =
            _state.value.copy(
                loading = true,
                message = null,
                isError = false
            )

        viewModelScope.launch {

            when (
                val result =
                    revokeTrustedDeviceUseCase()
            ) {

                is AppResult.Success -> {

                    _state.value =
                        TrustedDeviceUiState(
                            enrolled = false,
                            bound = false,
                            deviceId = null,
                            message =
                                "Trusted device was revoked.",
                            isError = false,
                            requireLogin = true
                        )
                }

                is AppResult.Failure -> {

                    _state.value =
                        _state.value.copy(
                            loading = false,
                            message =
                                errorMessage(
                                    result.error
                                ),
                            isError = true
                        )
                }
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

    private fun errorMessage(
        error: TrustedDeviceError
    ): String {

        return when (error) {

            TrustedDeviceError.CurrentUserUnavailable ->
                "Unable to verify current user."

            TrustedDeviceError.NotRegistered ->
                "Trusted device is not registered."

            TrustedDeviceError.PrivateKeyMissing ->
                "Trusted device private key is missing."

            TrustedDeviceError.InvalidRequest ->
                "Invalid trusted device request."

            TrustedDeviceError.InvalidCredentials ->
                "Password or MFA code is incorrect."

            TrustedDeviceError.AlreadyRegistered ->
                "This key is already registered."

            TrustedDeviceError.Unauthorized ->
                "Trusted device authorization failed."

            TrustedDeviceError.NetworkUnavailable ->
                "Unable to connect to the server."

            TrustedDeviceError.ServerUnavailable ->
                "Authentication server is unavailable."

            TrustedDeviceError.InvalidServerResponse ->
                "Invalid trusted device response."

            TrustedDeviceError.Unknown ->
                "Trusted device operation failed."
        }
    }
}