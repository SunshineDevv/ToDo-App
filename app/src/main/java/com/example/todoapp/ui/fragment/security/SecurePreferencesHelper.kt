package com.example.todoapp.ui.fragment.security

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import org.apache.commons.codec.binary.Base32

object SecurePreferencesHelper {

    private const val PREFS_NAME = "secure_prefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveActiveSession(context: Context, sessionId: String){
        getPrefs(context)
            .edit()
            .putString("encrypted_active_session_${FirebaseAuth.getInstance().currentUser?.uid}", sessionId)
            .apply()
    }

    fun getActiveSession(context: Context): String? {
        return getPrefs(context).getString("encrypted_active_session_${FirebaseAuth.getInstance().currentUser?.uid}", null)
    }

    fun clearActiveSession(context: Context) {
        getPrefs(context)
            .edit()
            .remove("encrypted_active_session_${FirebaseAuth.getInstance().currentUser?.uid}")
            .apply()
    }

    fun saveSuccess(context: Context, success: String) {
        val successBytes = success.toByteArray(Charsets.UTF_8)
        val encryptedSuccess = Base32().encodeToString(successBytes).replace("=", "")
        getPrefs(context).edit()
            .putString("encrypted_success_${FirebaseAuth.getInstance().currentUser?.uid}", encryptedSuccess)
            .apply()
    }

    fun getSuccess(context: Context): String {
        val encryptedSuccess = getPrefs(context).getString("encrypted_success_${FirebaseAuth.getInstance().currentUser?.uid}", null)

        if (encryptedSuccess.isNullOrEmpty()) return ""

        val decodedBytes = Base32().decode(encryptedSuccess)
        val decodedString = String(decodedBytes, Charsets.UTF_8)

        return decodedString
    }
}
