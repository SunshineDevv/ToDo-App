package com.example.todoapp.presentation.biometric

sealed interface BiometricAvailability {

    data object Available : BiometricAvailability

    data object NoHardware : BiometricAvailability

    data object HardwareUnavailable : BiometricAvailability

    data object NoneEnrolled : BiometricAvailability

    data object SecurityUpdateRequired : BiometricAvailability

    data object Unsupported : BiometricAvailability
}