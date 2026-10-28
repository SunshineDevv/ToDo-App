
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
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
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

    fun saveEnrollment(userId: String, deviceId: String, keyAlias: String) {
        require(userId.isNotBlank() && deviceId.isNotBlank() && keyAlias.isNotBlank())
        prefs.edit()
            .putString(deviceIdKey(userId), deviceId)
            .putString(keyAliasKey(userId), keyAlias)
            .apply()
    }

    fun getDeviceId(userId: String): String? = prefs.getString(deviceIdKey(userId), null)

    fun getKeyAlias(userId: String): String? = prefs.getString(keyAliasKey(userId), null)

    fun hasEnrollment(userId: String): Boolean {
        return !getDeviceId(userId).isNullOrBlank() && !getKeyAlias(userId).isNullOrBlank()
    }

    fun clearEnrollment(userId: String) {
        prefs.edit().remove(deviceIdKey(userId)).remove(keyAliasKey(userId)).apply()
    }

    fun beginSession(userId: String) {
        require(userId.isNotBlank())
        prefs.edit()
            .putString(KEY_SESSION_USER_ID, userId)
            .putBoolean(KEY_SESSION_BOUND, false)
            .apply()
    }

    fun getSessionUserId(): String? = prefs.getString(KEY_SESSION_USER_ID, null)

    fun isSessionBound(): Boolean = prefs.getBoolean(KEY_SESSION_BOUND, false)

    fun setSessionBound(userId: String, bound: Boolean) {
        check(getSessionUserId() == userId) { "Trusted Device session user mismatch" }
        prefs.edit().putBoolean(KEY_SESSION_BOUND, bound).apply()
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION_USER_ID).remove(KEY_SESSION_BOUND).apply()
    }

    private fun deviceIdKey(userId: String) = "user_${userId}_device_id"
    private fun keyAliasKey(userId: String) = "user_${userId}_key_alias"

    private companion object {
        const val PREFS_NAME = "trusted_device_state"
        const val KEY_SESSION_USER_ID = "session_user_id"
        const val KEY_SESSION_BOUND = "session_bound"
    }
}
