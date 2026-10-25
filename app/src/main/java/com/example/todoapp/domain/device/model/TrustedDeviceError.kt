package com.example.todoapp.domain.device.model

sealed interface TrustedDeviceError {

    data object CurrentUserUnavailable : TrustedDeviceError
    data object NotRegistered : TrustedDeviceError
    data object PrivateKeyMissing : TrustedDeviceError
    data object InvalidRequest : TrustedDeviceError
    data object InvalidCredentials : TrustedDeviceError
    data object AlreadyRegistered : TrustedDeviceError
    data object Unauthorized : TrustedDeviceError
    data object NetworkUnavailable : TrustedDeviceError
    data object ServerUnavailable : TrustedDeviceError
    data object InvalidServerResponse : TrustedDeviceError
    data object Unknown : TrustedDeviceError
}