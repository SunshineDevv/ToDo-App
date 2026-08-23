package com.example.todoapp.ui.fragment.auth.recoverpass

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.usecase.auth.ForgotPassUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ForgetPassViewModel @Inject constructor(
    private val forgotPassUseCase: ForgotPassUseCase
) : ViewModel() {

    val userEmail = MutableStateFlow("")

    private val _resetState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val resetState = _resetState.asStateFlow()

    fun resetPassword(userEmail: String) {
        val trimmedEmail = userEmail.trim()

        if (trimmedEmail.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("Email cannot be empty.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _resetState.value = AuthenticationState.ErrorReset("Invalid email format.")
            return
        }

        _resetState.value = AuthenticationState.Loading

        viewModelScope.launch {
            try {
                val response = forgotPassUseCase(trimmedEmail)

                val message = response.message
                    ?: "If an account with this email exists, password reset instructions have been sent."

                Log.i("BACKEND_RESET", "password reset request completed")

                _resetState.value = AuthenticationState.PasswordResetRequested(
                    message = message,
                    devResetToken = response.devResetToken
                )

            } catch (e: HttpException) {
                Log.e("BACKEND_RESET", "forgot password failed: HTTP ${e.code()}", e)
                _resetState.value = AuthenticationState.ErrorReset(mapHttpError(e))
            } catch (e: IOException) {
                Log.e("BACKEND_RESET", "forgot password failed: connection error", e)
                _resetState.value = AuthenticationState.ErrorReset(
                    "Cannot connect to authentication server."
                )
            } catch (e: Exception) {
                Log.e("BACKEND_RESET", "forgot password failed: ${e.message}", e)
                _resetState.value = AuthenticationState.ErrorReset(
                    "Password reset request failed."
                )
            }
        }
    }

    private fun mapHttpError(e: HttpException): String {
        return when (e.code()) {
            400 -> "Invalid email."
            404 -> "Password reset endpoint was not found."
            429 -> "Too many password reset attempts. Please wait and try again."
            500 -> "Authentication server error."
            else -> "Password reset request failed: HTTP ${e.code()}"
        }
    }

    fun clearState() {
        _resetState.value = AuthenticationState.Empty
    }
}