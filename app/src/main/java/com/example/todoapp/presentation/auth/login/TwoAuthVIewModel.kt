package com.example.todoapp.presentation.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.todoapp.presentation.auth.state.AuthenticationState
import com.example.todoapp.data.legacy.firebase.FirestoreDataManager
import com.example.todoapp.data.security.local.SecurePreferencesHelper
import com.example.todoapp.domain.security.service.UnifiedOtpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        _twoAuthState.value = AuthenticationState.Loading

        otpManager.validateToken(userInputCode) { isValid ->
            if (isValid) {
                SecurePreferencesHelper.saveSuccess(context, "true")
                FirestoreDataManager.saveSessionId(context)
                _twoAuthState.value = AuthenticationState.Success
            } else {
                SecurePreferencesHelper.saveSuccess(context, "false")
                failedAttempts++
                _twoAuthState.value = AuthenticationState.Error("❌ Invalid code! Try again $failedAttempts from $maxAttempts")

                if (failedAttempts >= maxAttempts) {
                    _twoAuthState.value = AuthenticationState.FatalError("🚫 You have exhausted all attempts! You have been logged out.")
                }
            }
        }
    }


    fun clearState() {
        _twoAuthState.value = AuthenticationState.Empty
    }

}