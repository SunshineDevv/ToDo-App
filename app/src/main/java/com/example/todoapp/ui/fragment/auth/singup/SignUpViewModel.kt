package com.example.todoapp.ui.fragment.auth.singup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    val userName = MutableStateFlow("")
    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")
    val userConfirmPassword = MutableStateFlow("")

    private val _registrationState =
        MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val registrationState = _registrationState.asStateFlow()

    fun registerNewUser(
        email: String,
        password: String,
        name: String,
        confirmPassword: String
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        if (
            trimmedName.isEmpty() &&
            trimmedEmail.isEmpty() &&
            password.isEmpty() &&
            trimmedConfirmPassword.isEmpty()
        ) {
            _registrationState.value = AuthenticationState.Error("Credentials cannot be empty")
            return
        }

        if (trimmedName.isEmpty()) {
            _registrationState.value = AuthenticationState.Error("Name field cannot be empty")
            return
        }

        if (trimmedEmail.isEmpty()) {
            _registrationState.value = AuthenticationState.Error("Email field cannot be empty")
            return
        }

        if (password.isEmpty()) {
            _registrationState.value = AuthenticationState.Error("Password field cannot be empty")
            return
        }

        if (trimmedConfirmPassword.isEmpty()) {
            _registrationState.value = AuthenticationState.Error("Confirming field cannot be empty")
            return
        }

        if (password != trimmedConfirmPassword) {
            _registrationState.value = AuthenticationState.Error("Passwords do not match")
            return
        }

        _registrationState.value = AuthenticationState.Loading

        viewModelScope.launch {
            try {
                registerUseCase(
                    email = trimmedEmail,
                    password = password
                )

                Log.i("BACKEND_REGISTER", "registration completed")

                _registrationState.value = AuthenticationState.Success

            } catch (e: HttpException) {
                Log.e("BACKEND_REGISTER", "registration failed: HTTP ${e.code()}", e)
                _registrationState.value = AuthenticationState.Error(mapHttpError(e))
            } catch (e: IOException) {
                Log.e("BACKEND_REGISTER", "registration failed: connection error", e)
                _registrationState.value = AuthenticationState.Error(
                    "Cannot connect to authentication server."
                )
            } catch (e: Exception) {
                Log.e("BACKEND_REGISTER", "registration failed: ${e.message}", e)
                _registrationState.value = AuthenticationState.Error(
                    "Registration failed: ${e.message}"
                )
            }
        }
    }

    private fun mapHttpError(e: HttpException): String {
        return when (e.code()) {
            400 -> "Invalid registration data."
            401 -> "Registration is not authorized."
            404 -> "Registration endpoint was not found."
            409 -> "User with this email already exists."
            429 -> "Too many registration attempts. Please wait and try again."
            500 -> "Authentication server error."
            else -> "Registration failed: HTTP ${e.code()}"
        }
    }

    fun clearState() {
        _registrationState.value = AuthenticationState.Empty
    }
}