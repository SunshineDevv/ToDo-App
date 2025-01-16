package com.example.todoapp.ui.fragment.auth.login

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.floor
import kotlin.math.pow

class UnifiedOtpManager(
    val secretKey: ByteArray,
    private val codeDigits: Int = 6,
    private val algorithm: String = "HmacSHA256",
    private val timeStep: Long = 30,
    private val timeUnit: TimeUnit = TimeUnit.SECONDS
) {

    companion object {
        private const val SECRET_SIZE_BYTES = 10

        fun generateReadableSecret(length: Int = SECRET_SIZE_BYTES): String {
            val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
            val random = SecureRandom()
            return (1..length)
                .map { allowedChars[random.nextInt(allowedChars.length)] }
                .joinToString("")
        }
    }

    fun generateToken(): String {
        val counter = currentCounter()
        val hash = calculateHmac(secretKey, counter)
        val offset = (hash.last() and 0x0F).toInt()
        val binaryCode = ByteBuffer.wrap(hash.copyOfRange(offset, offset + 4)).int and 0x7FFFFFFF
        val otp = binaryCode % 10.0.pow(codeDigits).toInt()
        return otp.toString().padStart(codeDigits, '0')
    }

    fun validateToken(inputCode: String): Boolean {
        val currentCounter = currentCounter()
        return (-1..1).any { offset ->
            val counter = currentCounter + offset
            inputCode == generateTokenForCounter(counter)
        }
    }

    private fun generateTokenForCounter(counter: Long): String {
        val hash = calculateHmac(secretKey, counter)
        val offset = (hash.last() and 0x0F).toInt()
        val binaryCode = ByteBuffer.wrap(hash.copyOfRange(offset, offset + 4)).int and 0x7FFFFFFF
        val otp = binaryCode % 10.0.pow(codeDigits).toInt()
        return otp.toString().padStart(codeDigits, '0')
    }

    fun buildOtpUri(account: String, issuer: String): String {
        val encodedSecret = Base32().encodeToString(secretKey)
        return "otpauth://totp/$issuer:$account?secret=$encodedSecret&issuer=$issuer&algorithm=SHA256&digits=$codeDigits&period=$timeStep"
    }

    fun generateQrCode(content: String): Bitmap {
        val size = 180
        val qrCodeWriter = QRCodeWriter()
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L)

        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun calculateHmac(key: ByteArray, counter: Long): ByteArray {
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key, "RAW"))
        val message = ByteBuffer.allocate(8).putLong(counter).array()
        return mac.doFinal(message)
    }

    private fun currentCounter(): Long {
        return floor(System.currentTimeMillis().toDouble() / timeUnit.toMillis(timeStep)).toLong()
    }
}
