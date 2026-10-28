package com.example.todoapp.domain.biometric.repository

interface BiometricLockRepository {

    fun isEnabled(): Boolean

    fun setEnabled(enabled: Boolean)
}