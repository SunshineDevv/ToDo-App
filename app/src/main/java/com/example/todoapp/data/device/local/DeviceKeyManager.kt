package com.example.todoapp.data.device.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceKeyManager @Inject constructor() {

    private val keyStore: KeyStore
        get() = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }

    fun hasKey(alias: String): Boolean {
        return keyStore.containsAlias(alias)
    }

    /**
     * Создаёт P-256 key pair, если ключа с таким alias ещё нет.
     *
     * Возвращает public key в формате:
     * X.509 SubjectPublicKeyInfo DER -> Base64.
     */
    fun ensureKeyPair(alias: String): String {
        require(alias.isNotBlank()) {
            "Key alias must not be blank"
        }

        if (!hasKey(alias)) {
            generateKeyPair(alias)
        }

        return getPublicKeyBase64(alias)
    }

    fun getPublicKeyBase64(alias: String): String {
        val certificate = keyStore.getCertificate(alias)
            ?: throw IllegalStateException(
                "No key found in Android Keystore for alias=$alias"
            )

        val publicKey = certificate.publicKey

        require(publicKey.algorithm.equals("EC", ignoreCase = true)) {
            "Expected EC public key but got ${publicKey.algorithm}"
        }

        if (publicKey is ECPublicKey) {
            require(publicKey.params.curve.field.fieldSize == 256) {
                "Expected P-256 public key"
            }
        }

        // publicKey.encoded для Android EC key =
        // X.509 SubjectPublicKeyInfo DER.
        return Base64.encodeToString(
            publicKey.encoded,
            Base64.NO_WRAP
        )
    }

    /**
     * SHA256withECDSA возвращает ASN.1 DER encoded ECDSA signature.
     *
     * Именно этот формат ожидает наш backend.
     */
    fun signUtf8(
        alias: String,
        payload: String
    ): String {
        val privateKey = keyStore.getKey(alias, null) as? java.security.PrivateKey
            ?: throw IllegalStateException(
                "No private key found for alias=$alias"
            )

        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)

        signature.initSign(privateKey)
        signature.update(payload.toByteArray(Charsets.UTF_8))

        val derSignature = signature.sign()

        return Base64.encodeToString(
            derSignature,
            Base64.NO_WRAP
        )
    }

    fun deleteKey(alias: String) {
        if (hasKey(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    private fun generateKeyPair(alias: String) {
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEY_STORE
        )

        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(
                ECGenParameterSpec(P_256_CURVE)
            )
            .setDigests(
                KeyProperties.DIGEST_SHA256
            )
            /*
             * Не требуем BiometricPrompt при каждом использовании.
             *
             * Это принципиально важно для автоматического background refresh.
             * Биометрию при необходимости добавим отдельно.
             */
            .setUserAuthenticationRequired(false)
            .build()

        generator.initialize(spec)
        generator.generateKeyPair()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val P_256_CURVE = "secp256r1"
        const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    }
}