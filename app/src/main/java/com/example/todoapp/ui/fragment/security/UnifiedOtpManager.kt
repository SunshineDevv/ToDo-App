package com.example.todoapp.ui.fragment.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
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
    private val timeStep = 30L
    private val timeUnit = TimeUnit.SECONDS

    private val sizeOfSecretInBytes = MutableStateFlow(0)

    private val secretFlow = MutableStateFlow<ByteArray?>(null)

    private val algorithm = MutableStateFlow("")

    init {
        initAlgorithmFromPrefs()
    }

    private fun initAlgorithmFromPrefs() {
        val savedAlgorithm = SecurePreferencesHelper.getEnum(context)
        if (savedAlgorithm.isNotEmpty()) {
            algorithm.value = savedAlgorithm
            sizeOfSecretInBytes.value = ShaAlgorithm.entries.find { it.algorithm == algorithm.value }?.sizeOfSecret ?: 32
        }
    }

    fun updateAlgorithm(newAlgorithm: ShaAlgorithm) {
        algorithm.value = newAlgorithm.algorithm
        sizeOfSecretInBytes.value = newAlgorithm.sizeOfSecret
        SecurePreferencesHelper.saveEnum(context, newAlgorithm)
    }

    fun clearAlgorithm(){
        algorithm.value = ""
    }

    fun updateSecret(newSecret: String) {
        val encodedSecret = Base32().decode(newSecret.uppercase().replace("=", ""))
        secretFlow.value = encodedSecret
    }

    fun generateReadableSecret(): String {
        if (algorithm.value == ""){
            algorithm.value = ShaAlgorithm.SHA256.algorithm
            sizeOfSecretInBytes.value = ShaAlgorithm.SHA256.sizeOfSecret
        }
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val random = SecureRandom()
        return (1..sizeOfSecretInBytes.value)
            .map { allowedChars[random.nextInt(allowedChars.length)] }
            .joinToString("")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun tokenFlow(): Flow<String?> {
        return algorithm.flatMapLatest {
            secretFlow.flatMapLatest { secret ->
                flow {
                    if (secret != null) {
                        if (secret.isEmpty()) {
                            emit("")
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
            }
        }.flowOn(Dispatchers.Default)
    }

    fun takeSecret(): ByteArray {
        val secret = SecurePreferencesHelper.getSecret(context) ?: return ByteArray(0)
        val decryptedSecret = KeystoreHelper.decryptData(secret)

        return try {
            val decodedSecret = Base32().decode(decryptedSecret)
            secretFlow.value = decodedSecret
            decodedSecret
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    fun validateToken(inputCode: String): Boolean {
        val secretKey = takeSecret()

        if (secretKey.isEmpty()) return false
        val currentCounter = currentCounter()

        return (-1..1).any { offset ->
            inputCode == generateTokenForCounter(secretKey, currentCounter + offset)
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
        val algorithm = algorithm.value.removePrefix("Hmac")
        return "otpauth://totp/$issuer:$account?secret=$encodedSecret&issuer=$issuer&algorithm=$algorithm&digits=$codeDigits&period=$timeStep"
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
        if (algorithm.value == ""){
            algorithm.value = ShaAlgorithm.SHA256.algorithm
            sizeOfSecretInBytes.value = ShaAlgorithm.SHA256.sizeOfSecret
        }
        val mac = Mac.getInstance(algorithm.value)
        mac.init(SecretKeySpec(key, algorithm.value))
        return mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array())
    }

    private fun currentCounter(): Long {
        return System.currentTimeMillis() / timeUnit.toMillis(timeStep)
    }
}