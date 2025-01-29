package com.example.todoapp.ui.fragment.security

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.repository.UserRepository
import com.example.todoapp.ui.fragment.security.securitystate.SecurityState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Base32
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: UserRepository,
    private val otpManager: UnifiedOtpManager
) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _securityState = MutableStateFlow<SecurityState>(SecurityState.Empty)
    val securityState = _securityState.asStateFlow()

    private val _secretKey = MutableStateFlow("")
    val secretKey = _secretKey.asStateFlow()

    private val _isSecure = MutableStateFlow(0)
    val isSecure = _isSecure.asStateFlow()

    private val _token = MutableStateFlow("")
    val token = _token.asStateFlow()



    fun onStart() {
        viewModelScope.launch {
            initValues()
            initValuesToSet()
        }
    }

    private suspend fun initValues(){
        if (userId != null) {
            val secure = repository.isUserSecure(userId)
            _isSecure.value = secure
            val secretKey = otpManager.takeSecret()
            _secretKey.value = String(secretKey, Charsets.UTF_8)
        }
    }

    private suspend fun initValuesToSet(){
        _isSecure.collectLatest { secure ->
            if (secure == 1) {
                collectToken()

                val secret = otpManager.takeSecret()
                val base32secret = Base32().encodeToString(secret).uppercase().replace("=", "")
                val otpUri = otpManager.buildOtpUri(
                    FirebaseAuth.getInstance().currentUser?.email.toString(),
                    "MyNotes"
                )
                _securityState.value =
                    SecurityState.LoadingData(String(secret, Charsets.UTF_8), base32secret, otpUri)
            }
        }
    }

    private fun collectToken(){
        viewModelScope.launch {
            otpManager.tokenFlow().collect {
                if (it != null) {
                    _token.value = it
                }
            }
        }
    }

    fun generateNewSecret() {
        viewModelScope.launch {

            if (userId != null) {
                repository.markUserAsSecure(userId)
            }

            _isSecure.value = 1

            val readableSecret = otpManager.generateReadableSecret()
            _secretKey.value = readableSecret
            otpManager.updateSecret(readableSecret)

            val base32Secret =
                Base32().encodeToString(readableSecret.toByteArray()).uppercase()
                    .replace("=", "")
            val encryptedSecret = KeystoreHelper.encryptData(base32Secret).toHex()
            SecurePreferencesHelper.saveSecret(context, encryptedSecret.hexToByteArray())

            val otpUri = otpManager.buildOtpUri(
                FirebaseAuth.getInstance().currentUser?.email.toString(),
                "MyNotes"
            )

            _securityState.value = SecurityState.LoadingData(secretKey.value, base32Secret, otpUri)
        }
    }

    fun setCustomSecret(userSecret: String) {
        if (userSecret.length < 16 ||  userSecret.length > 16) {
            _securityState.value =
                SecurityState.Error("❗️The secret must contain 16 characters.")
            return
        } else if (!userSecret.matches(Regex("^[A-Z2-7]+$"))) {
            _securityState.value =
                SecurityState.Error("❗️The secret must contain only the letters A-Z and the numbers 2-7")
            return
        } else {
            viewModelScope.launch {
                userId?.let {
                    repository.markUserAsSecure(it)
                }
            }
            _isSecure.value = 1
            _secretKey.value = userSecret
            otpManager.updateSecret(userSecret)

            val base32Secret = Base32().encodeToString(userSecret.toByteArray()).uppercase().replace("=", "")
            val encryptedSecret = KeystoreHelper.encryptData(base32Secret).toHex()
            SecurePreferencesHelper.saveSecret(context, encryptedSecret.hexToByteArray())

            val otpUri = otpManager.buildOtpUri(
                FirebaseAuth.getInstance().currentUser?.email.toString(),
                "MyNotes"
            )

            _securityState.value = SecurityState.LoadingData(secretKey.value, base32Secret, otpUri)
        }
    }


    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun String.hexToByteArray(): ByteArray {
        val len = this.length
        val result = ByteArray(len / 2)

        for (i in 0 until len step 2) {
            result[i / 2] = ((this[i].digitToInt(16) shl 4) + this[i + 1].digitToInt(16)).toByte()
        }
        return result
    }

    fun setSecureDisable() {
        SecurePreferencesHelper.clearSecret(context)
        _isSecure.value = 0
        viewModelScope.launch {
            if (userId != null) {
                repository.markUserAsNotSecure(userId)
            }
        }
    }

    fun clearState() {
        _securityState.value = SecurityState.Empty
    }
}