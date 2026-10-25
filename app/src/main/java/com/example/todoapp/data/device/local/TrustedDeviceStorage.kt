package com.example.todoapp.data.device.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrustedDeviceStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveEnrollment(
        userId: String,
        deviceId: String,
        keyAlias: String
    ) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_DEVICE_ID, deviceId)
            .putString(KEY_ALIAS, keyAlias)
            .putBoolean(KEY_DEVICE_BOUND, false)
            .apply()
    }

    fun markBound() {
        prefs.edit()
            .putBoolean(KEY_DEVICE_BOUND, true)
            .apply()
    }

    fun markUnbound() {
        prefs.edit()
            .putBoolean(KEY_DEVICE_BOUND, false)
            .apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }

    fun getKeyAlias(): String? {
        return prefs.getString(KEY_ALIAS, null)
    }

    fun isDeviceBound(): Boolean {
        return prefs.getBoolean(KEY_DEVICE_BOUND, false)
    }

    fun hasEnrollment(): Boolean {
        return !getUserId().isNullOrBlank() &&
                !getDeviceId().isNullOrBlank() &&
                !getKeyAlias().isNullOrBlank()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "trusted_device_state"

        const val KEY_USER_ID = "user_id"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_ALIAS = "key_alias"
        const val KEY_DEVICE_BOUND = "device_bound"
    }
}