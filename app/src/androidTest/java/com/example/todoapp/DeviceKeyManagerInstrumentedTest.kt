package com.example.todoapp

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.data.device.local.DeviceKeyManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyFactory
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

@RunWith(AndroidJUnit4::class)
class DeviceKeyManagerInstrumentedTest {

    private val keyManager = DeviceKeyManager()

    private val alias = "trusted_device_test_key"

    @Before
    fun setUp() {
        keyManager.deleteKey(alias)
    }

    @After
    fun tearDown() {
        keyManager.deleteKey(alias)
    }

    @Test
    fun generateP256KeyAndVerifySignature() {
        val publicKeyBase64 =
            keyManager.ensureKeyPair(alias)

        assertTrue(
            keyManager.hasKey(alias)
        )

        assertFalse(
            publicKeyBase64.isBlank()
        )

        /*
         * Проверяем, что Base64 действительно содержит
         * X.509 SubjectPublicKeyInfo DER.
         */
        val publicKeyDer = Base64.decode(
            publicKeyBase64,
            Base64.NO_WRAP
        )

        val publicKey = KeyFactory
            .getInstance("EC")
            .generatePublic(
                X509EncodedKeySpec(publicKeyDer)
            )

        assertEquals(
            "EC",
            publicKey.algorithm
        )

        val ecPublicKey = publicKey as ECPublicKey

        assertEquals(
            256,
            ecPublicKey.params.curve.field.fieldSize
        )

        /*
         * Пример payload.
         *
         * Сейчас это НЕ backend canonical payload.
         * На данном этапе тестируем только криптографический контракт.
         */
        val payload =
            """{"version":1,"purpose":"test","challenge":"abc123"}"""

        val signatureBase64 =
            keyManager.signUtf8(
                alias = alias,
                payload = payload
            )

        val signatureBytes = Base64.decode(
            signatureBase64,
            Base64.NO_WRAP
        )

        /*
         * Проверяем подпись публичным ключом.
         */
        val verifier = Signature.getInstance(
            "SHA256withECDSA"
        )

        verifier.initVerify(publicKey)
        verifier.update(
            payload.toByteArray(Charsets.UTF_8)
        )

        assertTrue(
            verifier.verify(signatureBytes)
        )
    }
}