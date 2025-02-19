package com.example.todoapp.database.repository

import android.annotation.SuppressLint
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object FirestoreSecurityHelper {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val FIRESTORE_COLLECTION = "users"
    private const val SECRET_FIELD = "secret_key_aes"
    private const val KEY_SIZE = 256

    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getOrCreateSecretKey(callback: (SecretKey?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(null)
            return
        }

        val userDoc = firestore.collection(FIRESTORE_COLLECTION)
            .document(userId)
        userDoc.get()
            .addOnSuccessListener { document ->
            if (document.exists() && document.contains(SECRET_FIELD)) {
                val keyString = document.getString(SECRET_FIELD)
                if (!keyString.isNullOrEmpty()) {
                    val keyBytes = Base64.decode(keyString, Base64.DEFAULT)
                    val secretKey = SecretKeySpec(keyBytes, "AES")
                    callback(secretKey)
                    return@addOnSuccessListener
                }
            }
            val newKey = generateSecretKey()
            val encodedKey = Base64.encodeToString(newKey.encoded, Base64.DEFAULT)
            userDoc.set(mapOf(SECRET_FIELD to encodedKey), SetOptions.merge())
                .addOnSuccessListener { callback(newKey) }
                .addOnFailureListener { callback(null) }
        }.addOnFailureListener {
            callback(null)
        }
    }

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }

    fun encryptData(plainText: String, callback: (ByteArray?) -> Unit) {
        getOrCreateSecretKey { secretKey ->
            if (secretKey == null) {
                callback(null)
                return@getOrCreateSecretKey
            }
            try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                callback(iv + encryptedBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }

    fun decryptData(encryptedData: ByteArray, callback: (String?) -> Unit) {
        getOrCreateSecretKey { secretKey ->
            if (secretKey == null) {
                callback(null)
                return@getOrCreateSecretKey
            }
            try {
                val iv = encryptedData.copyOfRange(0, 12)
                val encryptedBytes = encryptedData.copyOfRange(12, encryptedData.size)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
                val decryptedText = String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
                callback(decryptedText)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }
}