package com.example.todoapp.ui.fragment.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.experimental.and
import kotlin.math.pow

class UnifiedOtpManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val codeDigits = 6
    private val algorithm = "HmacSHA256"
    private val timeStep = 30L
    private val timeUnit = TimeUnit.SECONDS

    private val sizeOfSecretInBytes = 16

    private val secretFlow = MutableStateFlow<ByteArray?>(null)

    fun updateSecret(newSecret: String) {
        val encodedSecret = Base32().decode(newSecret.uppercase().replace("=", ""))
        secretFlow.value = encodedSecret
    }

    fun generateReadableSecret(length: Int = sizeOfSecretInBytes): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val random = SecureRandom()
        val secret = (1..length)
            .map { allowedChars[random.nextInt(allowedChars.length)] }
            .joinToString("")
        return secret
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun tokenFlow(): Flow<String?> {
        return secretFlow.flatMapLatest { secret ->
            flow {
                if (secret != null) {
                    if (secret.isEmpty()) {
                        emit("000000")
                        return@flow
                    }
                }
                while (true) {
                    val currentTime = System.currentTimeMillis()
                    val currentCounter = currentTime / timeUnit.toMillis(timeStep)

                    val currentToken = secret?.let { generateTokenForCounter(it, currentCounter) }
                    emit(currentToken)

                    val nextExecutionTime =
                        ((currentTime / timeUnit.toMillis(timeStep)) + 1) * timeUnit.toMillis(
                            timeStep
                        )
                    delay(nextExecutionTime - currentTime)
                }
            }
        }.flowOn(Dispatchers.Default)
    }

    fun takeSecret(): ByteArray {
        val secret = SecurePreferencesHelper.getSecret(context) ?: return ByteArray(0)
        val decryptedSecret = KeystoreHelper.decryptData(secret) ?: return ByteArray(0)

        return try {
            val decodedSecret = Base32().decode(decryptedSecret)
            secretFlow.value = decodedSecret
            decodedSecret
        } catch (e: Exception) {
            ByteArray(0)  // Возвращаем пустой массив при ошибке декодирования
        }
    }

    fun validateToken(inputCode: String): Boolean {
        val secretKey = takeSecret()

        if (secretKey == null || secretKey.isEmpty()) {
            Log.w("OTP_Validation", "⚠️ Warning: Secret key is missing or empty.")
            return false
        }

        val currentCounter = currentCounter()

        Log.i("OTP_Validation", "🔍 Incoming code: $inputCode")
        Log.i("OTP_Validation", "🕒 Current counter: $currentCounter")

        return (-1..1).any { offset ->
            val generatedToken = generateTokenForCounter(secretKey, currentCounter + offset)

            Log.d(
                "OTP_Validation",
                "🔄 Checking generated token: $generatedToken (offset = $offset, counter = ${currentCounter + offset})"
            )

            val isMatch = inputCode == generatedToken
            if (isMatch) {
                Log.i("OTP_Validation", "✅ Token matched! Authentication successful.")
            }

            isMatch
        }.also { result ->
            if (!result) {
                Log.e("OTP_Validation", "❌ Error: The entered code does not match any valid OTPs.")
            }
        }
    }

    private fun generateTokenForCounter(secretKey: ByteArray, counter: Long): String {
        val hash = calculateHmac(secretKey, counter)
        val offset = (hash.last() and 0x0F).toInt()
        val otp =
            (ByteBuffer.wrap(hash.copyOfRange(offset, offset + 4)).int and 0x7FFFFFFF) % 10.0.pow(
                codeDigits
            ).toInt()

        return otp.toString().padStart(codeDigits, '0')
    }

    fun buildOtpUri(account: String, issuer: String): String {
        val secretKey = takeSecret()
        if (secretKey.isEmpty()) return ""
        val encodedSecret = Base32().encodeToString(secretKey).replace("=", "")
        return "otpauth://totp/$issuer:$account?secret=$encodedSecret&issuer=$issuer&algorithm=SHA256&digits=$codeDigits&period=$timeStep"
    }

    fun generateQrCode(content: String): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            val size = 180
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
            Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateHmac(key: ByteArray, counter: Long): ByteArray {
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key, algorithm))
        return mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array())
    }

    private fun currentCounter(): Long {
        return System.currentTimeMillis() / timeUnit.toMillis(timeStep)
    }
}