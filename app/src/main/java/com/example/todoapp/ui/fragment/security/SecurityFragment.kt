package com.example.todoapp.ui.fragment.security

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.todoapp.databinding.FragmentSecurityBinding
import org.apache.commons.codec.binary.Base32

class SecurityFragment : Fragment() {

    private var binding: FragmentSecurityBinding? = null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var otpManager: UnifiedOtpManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecurityBinding.inflate(inflater,container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Генерація ключа в Keystore
        KeystoreHelper.generateSecretKey()

        // Завантаження збереженого секрету при запуску
        loadSavedSecret()

        binding?.generateSecretButton?.setOnClickListener {
            generateNewSecret()
        }

        binding?.setCustomSecretButton?.setOnClickListener {
            setCustomSecret()
        }

        startCodeUpdater()

    }

    // Завантаження зашифрованого секрету з Keystore
    private fun loadSavedSecret() {
        val encryptedSecret = SecurePreferencesHelper.getSecret(requireContext())
        if (encryptedSecret != null) {
            try {
                val decryptedSecret = KeystoreHelper.decryptData(encryptedSecret)
                Log.i("SECRET_MY", "$decryptedSecret")
                otpManager = UnifiedOtpManager(Base32().decode(decryptedSecret))
                binding?.secretEditText?.setText(String(Base32().decode(decryptedSecret)))
                updateUI()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Не вдалося розшифрувати секрет!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateNewSecret() {
        // Генерація читабельного секрету
        val readableSecret = UnifiedOtpManager.generateReadableSecret()

        // Кодуємо секрет у Base32 для безпечного зберігання
        val base32Secret = Base32().encodeToString(readableSecret.toByteArray()).replace("=", "")

        // Шифруємо Base32-кодований секрет і зберігаємо
        val encryptedSecret = KeystoreHelper.encryptData(base32Secret).toHex()
        SecurePreferencesHelper.saveSecret(requireContext(), encryptedSecret.hexToByteArray())

        // Ініціалізуємо OTP-менеджер із закодованим секретом
        otpManager = UnifiedOtpManager(Base32().decode(base32Secret))

        // Відображаємо читабельний секрет і закодований секрет у відповідних полях
        binding?.secretEditText?.setText(readableSecret)         // Читабельний секрет для користувача
        binding?.base32SecretEditText?.setText(base32Secret)     // Base32-кодований секрет

        updateUI()
    }

    private fun setCustomSecret() {
        val userSecret = binding?.secretEditText?.text.toString().trim().uppercase()

        if (userSecret.length < 10) {
            Toast.makeText(requireContext(), "❗️Секрет повинен містити щонайменше 10 символів", Toast.LENGTH_SHORT).show()
            return
        }

        if (!userSecret.matches(Regex("^[A-Z2-7]+$"))) {
            Toast.makeText(requireContext(), "❗️Секрет повинен містити лише літери A-Z та цифри 2-7", Toast.LENGTH_SHORT).show()
            return
        }

        val base32Secret = Base32().encodeToString(userSecret.toByteArray()).replace("=", "")
        val encryptedSecret = KeystoreHelper.encryptData(base32Secret)
        Log.i("SECRET_MY", "sAVE: $base32Secret")
        SecurePreferencesHelper.saveSecret(requireContext(), encryptedSecret)

        otpManager = UnifiedOtpManager(Base32().decode(base32Secret))
        binding?.base32SecretEditText?.setText(base32Secret)

        val otpUri = otpManager.buildOtpUri("user@example.com", "MyNotes")
        binding?.qrCodeImageView?.setImageBitmap(otpManager.generateQrCode(otpUri))

        Toast.makeText(requireContext(), "✅ Власний секрет встановлено!", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        if (::otpManager.isInitialized) {
            val base32Secret = Base32().encodeToString(otpManager.secretKey)
            binding?.base32SecretEditText?.setText(base32Secret)

            val token = otpManager.generateToken()
            binding?.codeEditText?.setText(token)

            val otpUri = otpManager.buildOtpUri("user@example.com", "MyNotes")
            binding?.qrCodeImageView?.setImageBitmap(otpManager.generateQrCode(otpUri))
        }
    }

    // Автоматичне оновлення коду кожні 30 секунд
    private fun startCodeUpdater() {
        handler.post(object : Runnable {
            override fun run() {
                if (::otpManager.isInitialized) {
                    updateUI()
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    fun String.hexToByteArray(): ByteArray {
        val len = this.length
        val result = ByteArray(len / 2)

        for (i in 0 until len step 2) {
            result[i / 2] = ((this[i].digitToInt(16) shl 4) + this[i + 1].digitToInt(16)).toByte()
        }
        return result
    }

}