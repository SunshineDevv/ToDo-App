package com.example.todoapp.ui.fragment.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")

    private val _logInState = MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val logInState = _logInState.asStateFlow()

    fun logInUser(email: String, password: String) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isEmpty() && password.isEmpty()) {
            _logInState.value = AuthenticationState.Error("Credential fields cannot be empty")
            return
        }

        if (trimmedEmail.isEmpty()) {
            _logInState.value = AuthenticationState.Error("Email field cannot be empty")
            return
        }

        if (password.isEmpty()) {
            _logInState.value = AuthenticationState.Error("Password field cannot be empty")
            return
        }

        _logInState.value = AuthenticationState.Loading

        viewModelScope.launch {
            try {
                loginUseCase(
                    email = trimmedEmail,
                    password = password
                )

                Log.i("BACKEND_AUTH", "login completed and tokens saved")

                _logInState.value = AuthenticationState.SuccessNoSecureEnable

            } catch (e: HttpException) {
                Log.e("BACKEND_AUTH", "login failed: HTTP ${e.code()}", e)
                _logInState.value = AuthenticationState.Error(mapHttpError(e))
            } catch (e: IOException) {
                Log.e("BACKEND_AUTH", "login failed: connection error", e)
                _logInState.value = AuthenticationState.Error(
                    "Cannot connect to authentication server."
                )
            } catch (e: Exception) {
                Log.e("BACKEND_AUTH", "login failed: ${e.message}", e)
                _logInState.value = AuthenticationState.Error(
                    "Authentication failed: ${e.message}"
                )
            }
        }
    }

    private fun mapHttpError(e: HttpException): String {
        return when (e.code()) {
            400 -> "Invalid email or password format."
            401 -> "Invalid credentials. Check your email and password."
            404 -> "Authentication endpoint was not found."
            409 -> "User with this email already exists."
            429 -> "Too many login attempts. Please wait and try again."
            500 -> "Authentication server error."
            else -> "Authentication failed: HTTP ${e.code()}"
        }
    }

    fun clearState() {
        _logInState.value = AuthenticationState.Empty
    }
}