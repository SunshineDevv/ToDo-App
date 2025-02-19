package com.example.todoapp.ui.fragment.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.repository.FirestoreDataManager
import com.example.todoapp.database.repository.FirestoreSecurityHelper
import com.example.todoapp.ui.fragment.security.securitystate.SecurityState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Base32
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val otpManager: UnifiedOtpManager
) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _securityState = MutableStateFlow<SecurityState>(SecurityState.Empty)
    val securityState = _securityState.asStateFlow()

    private val _secretKey = MutableStateFlow("")
    val secretKey = _secretKey.asStateFlow()

    private val _isSecure = MutableStateFlow(false)
    val isSecure = _isSecure.asStateFlow()

    private val _token = MutableStateFlow("")
    val token = _token.asStateFlow()

    private val _currentAlgorithm = MutableStateFlow(ShaAlgorithm.SHA256.algorithm)
    val currentAlgorithm = _currentAlgorithm.asStateFlow()

    fun onStart() {
        viewModelScope.launch {
            val savedAlgorithm = FirestoreDataManager.getAlgorithm()
            _currentAlgorithm.value =
                savedAlgorithm.takeUnless { it.isEmpty() } ?: ShaAlgorithm.SHA256.algorithm
            initValues()
            initValuesToSet()
        }
    }

    private suspend fun initValues() {
        if (userId != null) {
            val secure = FirestoreDataManager.getUserStatus()
            _isSecure.value = secure

            val secretKey = suspendCoroutine<ByteArray> { continuation ->
                otpManager.takeSecret { continuation.resume(it) }
            }

            _secretKey.value = String(secretKey, Charsets.UTF_8)
        }
    }


    private suspend fun initValuesToSet() {
        _isSecure.collectLatest { secure ->
            if (secure) {
                collectToken()

                val secret = suspendCoroutine { continuation ->
                    otpManager.takeSecret { continuation.resume(it) }
                }
                val base32secret = Base32().encodeToString(secret).uppercase().replace("=", "")

                val otpUri = suspendCoroutine { continuation ->
                    otpManager.buildOtpUri(
                        FirebaseAuth.getInstance().currentUser?.email.toString(),
                        "MyNotes"
                    ) { uri ->
                        continuation.resume(uri)
                    }
                }
                if (String(secret, Charsets.UTF_8).isNotEmpty()){
                    _securityState.value =
                        SecurityState.LoadingData(String(secret, Charsets.UTF_8), base32secret, otpUri)
                }
            }
        }
    }

    fun setAlgorithm(algorithm: ShaAlgorithm) {
        _currentAlgorithm.value = algorithm.algorithm
    }

    private fun collectToken() {
        viewModelScope.launch {
            otpManager.tokenFlow().collect {
                if (it != null) {
                    _token.value = it
                }
            }
        }
    }

    fun getSecureStatus(): Boolean {
        return isSecure.value
    }

    fun generateNewSecret() {
        _securityState.value = SecurityState.Loading

        if (currentAlgorithm.value.isEmpty()) {
            _currentAlgorithm.value = ShaAlgorithm.SHA256.algorithm
        }

        viewModelScope.launch {
            ShaAlgorithm.entries.find { it.algorithm == currentAlgorithm.value }
                ?.let {
                    FirestoreDataManager.saveAlgorithm(it)
                    otpManager.updateAlgorithm(it)
                }

            _isSecure.value = true
            val readableSecret = otpManager.generateReadableSecret()
            _secretKey.value = readableSecret
            otpManager.updateSecret(readableSecret)

            val base32Secret = Base32().encodeToString(readableSecret.toByteArray()).uppercase().replace("=", "")

            val encryptedSecret = suspendCoroutine { continuation ->
                FirestoreSecurityHelper.encryptData(base32Secret) { encrypted ->
                    continuation.resume(encrypted)
                }
            }
            if (encryptedSecret != null) {
                FirestoreDataManager.saveSecret(encryptedSecret)
                FirestoreDataManager.markUserStatus(true)

                if (secretKey.value.isNotEmpty() && secretKey.value.isNotBlank()) {
                    val otpUri = suspendCoroutine { continuation ->
                        otpManager.buildOtpUri(
                            FirebaseAuth.getInstance().currentUser?.email.toString(),
                            "MyNotes"
                        ) { uri ->
                            continuation.resume(uri)
                        }
                    }
                    _securityState.value = SecurityState.LoadingData(
                        secretKey.value,
                        base32Secret,
                        otpUri
                    )
                } else {
                    _securityState.value = SecurityState.Error("Failed to generate secret")
                }
            } else {
                _securityState.value = SecurityState.Error("Encryption failed")
            }
        }
    }

    fun setCustomSecret(userSecret: String) {
        val requiredLength = when (currentAlgorithm.value) {
            ShaAlgorithm.SHA1.algorithm -> ShaAlgorithm.SHA1.sizeOfSecret
            ShaAlgorithm.SHA256.algorithm -> ShaAlgorithm.SHA256.sizeOfSecret
            ShaAlgorithm.SHA512.algorithm -> ShaAlgorithm.SHA512.sizeOfSecret
            else -> ShaAlgorithm.SHA256.sizeOfSecret
        }

        if (userSecret.length != requiredLength) {
            _securityState.value = SecurityState.Error("❗️The secret must contain exactly $requiredLength characters.")
            return
        } else if (!userSecret.matches(Regex("^[A-Z2-7]+$"))) {
            _securityState.value = SecurityState.Error("❗️The secret must contain only the letters A-Z and the numbers 2-7.")
            return
        }

        _securityState.value = SecurityState.Loading

        if (currentAlgorithm.value.isEmpty()) {
            _currentAlgorithm.value = ShaAlgorithm.SHA256.algorithm
        }

        viewModelScope.launch {
            ShaAlgorithm.entries.find { it.algorithm == currentAlgorithm.value }
                ?.let {
                FirestoreDataManager.saveAlgorithm(it)
                otpManager.updateAlgorithm(it)
            }

            _isSecure.value = true
            _secretKey.value = userSecret
            otpManager.updateSecret(userSecret)

            val base32Secret = Base32().encodeToString(userSecret.toByteArray()).uppercase().replace("=", "")

            val encryptedSecret = suspendCoroutine { continuation ->
                FirestoreSecurityHelper.encryptData(base32Secret) { encrypted ->
                    continuation.resume(encrypted)
                }
            }

            if (encryptedSecret != null) {
                FirestoreDataManager.saveSecret(encryptedSecret)
                FirestoreDataManager.markUserStatus(true)

                val otpUri = suspendCoroutine{ continuation ->
                    otpManager.buildOtpUri(
                        FirebaseAuth.getInstance().currentUser?.email.toString(),
                        "MyNotes"
                    ) { uri ->
                        continuation.resume(uri)
                    }
                }

                _securityState.value = SecurityState.LoadingData(secretKey.value, base32Secret, otpUri)
            } else {
                _securityState.value = SecurityState.Error("❗️Failed to encrypt secret.")
            }
        }
    }

    fun setSecureDisable() {
        viewModelScope.launch {
            FirestoreDataManager.clearAlgorithm()
            FirestoreDataManager.clearSecret()
            FirestoreDataManager.markUserStatus(false)
        }
        _currentAlgorithm.value = ""
        _isSecure.value = false
        otpManager.clearAlgorithm()
        otpManager.updateSecret("")
    }

    fun clearState() {
        _securityState.value = SecurityState.Empty
    }
}