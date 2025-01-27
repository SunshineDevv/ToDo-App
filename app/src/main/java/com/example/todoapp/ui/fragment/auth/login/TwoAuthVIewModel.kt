package com.example.todoapp.ui.fragment.auth.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.repository.UserRepository
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.example.todoapp.ui.fragment.security.UnifiedOtpManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwoAuthVIewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val otpManager: UnifiedOtpManager
) : ViewModel() {

    val inputToken = MutableStateFlow("")

    private val _twoAuthState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val twoAuthState = _twoAuthState.asStateFlow()

    private var failedAttempts = 0
    private val maxAttempts = 3

    fun validateUserInputCode(userInputCode: String) {
        try {
            Log.i("CheckToken", otpManager.validateToken(userInputCode).toString())
            if (otpManager.validateToken(userInputCode)) {
                SecurePreferencesHelper.saveSuccess(context, "true")
                _twoAuthState.value = AuthenticationState.Success
            } else {
                SecurePreferencesHelper.saveSuccess(context, "false")
                failedAttempts++
                _twoAuthState.value = AuthenticationState.Error("❌ Invalid code! Try again $failedAttempts from $maxAttempts")
                if (failedAttempts >= maxAttempts) {
                    _twoAuthState.value = AuthenticationState.FatalError("🚫 You have exhausted all attempts! You have been logged out.")
                }
            }
        } catch (e: Exception) {
            SecurePreferencesHelper.saveSuccess(context, "false")
            _twoAuthState.value = AuthenticationState.FatalError("⚠️ An error occurred: ${e.message}")
        }
    }

    fun clearState() {
        _twoAuthState.value = AuthenticationState.Empty
    }

}