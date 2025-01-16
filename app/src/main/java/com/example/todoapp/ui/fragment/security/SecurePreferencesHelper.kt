package com.example.todoapp.ui.fragment.security

import android.content.Context
import android.content.SharedPreferences

object SecurePreferencesHelper {

    private const val PREFS_NAME = "secure_prefs"
    private const val SECRET_KEY = "encrypted_secret"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Збереження зашифрованого секрету
    fun saveSecret(context: Context, secret: ByteArray) {
        val encryptedSecret = secret.toHex()
        getPrefs(context).edit().putString(SECRET_KEY, encryptedSecret).apply()
    }

    // Отримання зашифрованого секрету
    fun getSecret(context: Context): ByteArray? {
        val encryptedSecretHex = getPrefs(context).getString(SECRET_KEY, null)
        return encryptedSecretHex?.decodeHex()
    }

    // Перетворення ByteArray в Hex
    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    // Перетворення Hex в ByteArray
    private fun String.decodeHex(): ByteArray {
        val result = ByteArray(length / 2)
        for (i in indices step 2) {
            result[i / 2] = ((this[i].digitToInt(16) shl 4) + this[i + 1].digitToInt(16)).toByte()
        }
        return result
    }
}
