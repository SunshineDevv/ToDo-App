package com.example.todoapp.ui.fragment.auth.recoverpass

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.usecase.auth.ResetPassUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ResetPassViewModel @Inject constructor(
    private val resetPassUseCase: ResetPassUseCase
) : ViewModel() {

    val resetToken = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _resetState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val resetState = _resetState.asStateFlow()

    fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val trimmedToken = token.trim()
        val rawNewPassword = newPassword
        val rawConfirmPassword = confirmPassword

        if (trimmedToken.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("Reset token cannot be empty.")
            return
        }

        if (rawNewPassword.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("New password cannot be empty.")
            return
        }

        if (rawConfirmPassword.isBlank()) {
            _resetState.value = AuthenticationState.ErrorReset("Confirm password cannot be empty.")
            return
        }

        if (rawNewPassword != rawConfirmPassword) {
            _resetState.value = AuthenticationState.ErrorReset("Passwords do not match.")
            return
        }

        _resetState.value = AuthenticationState.Loading

        viewModelScope.launch {
            try {
                val response = resetPassUseCase(
                    token = trimmedToken,
                    newPassword = rawNewPassword
                )

                val message = response.message ?: "Password has been reset successfully."

                Log.i("BACKEND_RESET", "password reset completed")

                _resetState.value = AuthenticationState.PasswordResetCompleted(message)

            } catch (e: HttpException) {
                Log.e("BACKEND_RESET", "reset password failed: HTTP ${e.code()}", e)
                _resetState.value = AuthenticationState.ErrorReset(mapHttpError(e))
            } catch (e: IOException) {
                Log.e("BACKEND_RESET", "reset password failed: connection error", e)
                _resetState.value = AuthenticationState.ErrorReset(
                    "Cannot connect to authentication server."
                )
            } catch (e: Exception) {
                Log.e("BACKEND_RESET", "reset password failed: ${e.message}", e)
                _resetState.value = AuthenticationState.ErrorReset(
                    "Password reset failed."
                )
            }
        }
    }

    private fun mapHttpError(e: HttpException): String {
        return when (e.code()) {
            400 -> "Reset token is invalid or expired."
            404 -> "Password reset endpoint was not found."
            429 -> "Too many password reset attempts. Please wait and try again."
            500 -> "Authentication server error."
            else -> "Password reset failed: HTTP ${e.code()}"
        }
    }

    fun clearState() {
        _resetState.value = AuthenticationState.Empty
    }
}