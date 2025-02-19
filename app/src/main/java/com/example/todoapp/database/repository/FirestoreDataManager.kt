package com.example.todoapp.database.repository

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import org.apache.commons.codec.binary.Base32
import android.util.Base64
import com.example.todoapp.ui.activity.AuthActivity
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.example.todoapp.ui.fragment.security.ShaAlgorithm
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID

object FirestoreDataManager {

    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    fun saveSessionId(context: Context){
        val userId = getUserId() ?: return

        val sessionId = UUID.randomUUID().toString()
        SecurePreferencesHelper.saveActiveSession(context, sessionId)
        userId.let {
            firestore.collection("users")
                .document(userId)
                .set(mapOf("activeSession" to sessionId), SetOptions.merge())
        }
    }

    suspend fun saveSecret(secret: ByteArray) {
        val userId = getUserId() ?: return
        val encryptedSecret = Base64.encodeToString(secret, Base64.NO_WRAP)

        try {
            firestore.collection("users")
                .document(userId)
                .set(mapOf("secret" to encryptedSecret), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun getSecret(): ByteArray? {
        val userId = getUserId() ?: return null

        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val encryptedSecret = document.getString("secret")
            if (encryptedSecret.isNullOrEmpty()) {
                return null
            }

            Base64.decode(encryptedSecret, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveAlgorithm(enumValue: ShaAlgorithm) {
        val userId = getUserId() ?: return
        val encryptedEnum = Base32().encodeToString(enumValue.algorithm.toByteArray(Charsets.UTF_8)).replace("=", "")

        try {
            firestore.collection("users")
                .document(userId)
                .set(mapOf("algorithm" to encryptedEnum), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAlgorithm(): String {
        val userId = getUserId() ?: return ""

        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val encryptedEnum = document.getString("algorithm") ?: return ""
            val decodedBytes = Base32().decode(encryptedEnum)
            String(decodedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun clearSecret() {
        val userId = getUserId() ?: return

        try {
            firestore.collection("users")
                .document(userId)
                .set(mapOf("secret" to ""), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearAlgorithm() {
        val userId = getUserId() ?: return

        try {
            firestore.collection("users")
                .document(userId)
                .set(mapOf("algorithm" to ""), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markUserStatus(isSecure: Boolean) {
        val userId = getUserId() ?: return
        val data = mapOf("isSecure" to isSecure)

        try {
            firestore.collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUserStatus(): Boolean {
        val userId = getUserId() ?: return false

        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            document.getBoolean("isSecure") ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun startSessionListener(activity: Activity) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val activeSession = snapshot.getString("activeSession")
                val localSession = SecurePreferencesHelper.getActiveSession(activity)

                if (activeSession != null && activeSession != localSession) {

                    FirebaseAuth.getInstance().signOut()
                    SecurePreferencesHelper.clearActiveSession(activity)
                    val intent = Intent(activity, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    activity.startActivity(intent)
                    activity.finish()
                }
            }
    }
}