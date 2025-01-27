package com.example.todoapp.ui.fragment.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.apache.commons.codec.binary.Base32

object SecurePreferencesHelper {

    private const val PREFS_NAME = "secure_prefs"

    private var auth = FirebaseAuth.getInstance()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSuccess(context: Context, success: String) {
        Log.i("SuccessCheck", success.toString())

        val successBytes = success.toByteArray(Charsets.UTF_8)
        val encryptedSuccess = Base32().encodeToString(successBytes).replace("=", "")
        Log.i("SuccessCheck", encryptedSuccess)
        getPrefs(context).edit()
            .putString("encrypted_success_${FirebaseAuth.getInstance().currentUser?.uid}", encryptedSuccess)
            .apply()
    }

    fun getSuccess(context: Context): String {
        val encryptedSuccess = getPrefs(context).getString("encrypted_success_${FirebaseAuth.getInstance().currentUser?.uid}", null)

        if (encryptedSuccess.isNullOrEmpty()) return ""

        val decodedBytes = Base32().decode(encryptedSuccess)
        val decodedString = String(decodedBytes, Charsets.UTF_8)
        Log.i("SuccessCheck", decodedString)

        return decodedString
    }

    fun saveSecret(context: Context, secret: ByteArray) {
        val encryptedSecret = secret.toHex()
        getPrefs(context).edit().putString("encrypted_secret_${auth.currentUser?.uid}", encryptedSecret).apply()
    }

    fun getSecret(context: Context): ByteArray? {
        val encryptedSecretHex = getPrefs(context).getString("encrypted_secret_${auth.currentUser?.uid}", null)
        return encryptedSecretHex?.decodeHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.decodeHex(): ByteArray {
        val result = ByteArray(length / 2)
        for (i in indices step 2) {
            result[i / 2] = ((this[i].digitToInt(16) shl 4) + this[i + 1].digitToInt(16)).toByte()
        }
        return result
    }
}
