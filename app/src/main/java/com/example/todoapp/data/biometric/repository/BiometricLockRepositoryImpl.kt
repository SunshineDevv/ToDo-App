package com.example.todoapp.data.biometric.repository

import android.content.Context
import com.example.todoapp.domain.biometric.repository.BiometricLockRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricLockRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : BiometricLockRepository {

    private val preferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    override fun isEnabled(): Boolean {
        return preferences.getBoolean(
            KEY_BIOMETRIC_LOCK_ENABLED,
            false
        )
    }

    override fun setEnabled(enabled: Boolean) {
        preferences
            .edit()
            .putBoolean(
                KEY_BIOMETRIC_LOCK_ENABLED,
                enabled
            )
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME =
            "biometric_lock_preferences"

        const val KEY_BIOMETRIC_LOCK_ENABLED =
            "biometric_lock_enabled"
    }
}