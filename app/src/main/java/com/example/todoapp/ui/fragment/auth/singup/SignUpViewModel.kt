package com.example.todoapp.ui.fragment.auth.singup

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.database.repository.FirestoreDataManager
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private lateinit var auth: FirebaseAuth

    val userName = MutableStateFlow("")
    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")
    val userConfirmPassword = MutableStateFlow("")

    private val _registrationState =
        MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val registrationState = _registrationState.asStateFlow()

    fun registerNewUser(email: String, password: String, name: String, confirmPassword: String) {
        _registrationState.value = AuthenticationState.Loading
        auth = Firebase.auth
        if (name.trim().isNotEmpty() && email.trim().isNotEmpty() && password.trim()
            .isNotEmpty() && confirmPassword.trim().isNotEmpty()
            ) {
            if (password == confirmPassword){
                Log.d("CheckPass", "$password and $confirmPassword")
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    viewModelScope.launch {
                                        try {
                                            SecurePreferencesHelper.saveSuccess(context, "")
                                            FirestoreDataManager.saveSessionId(context)
                                            _registrationState.value = AuthenticationState.Success
                                        } catch (e: Exception) {
                                            _registrationState.value =
                                                AuthenticationState.Error("Database error: ${e.message}")
                                        }
                                    }
                                } else {
                                    _registrationState.value =
                                        AuthenticationState.Error("Failed to update profile")
                                }
                            }
                        } else {
                            _registrationState.value =
                                AuthenticationState.Error("Authentication failed: ${task.exception?.message}")
                        }
                    }
            } else {
                _registrationState.value =
                    AuthenticationState.Error("Passwords do not match")
            }
        } else if (name.isBlank() && email.isBlank() && password.isBlank() && confirmPassword.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Credentials cannot be empty")
        } else if (name.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Name field cannot be empty")
        } else if (email.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Email field cannot be empty")
        } else if (password.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Password field cannot be empty")
        } else if (confirmPassword.isBlank()) {
            _registrationState.value = AuthenticationState.Error("Confirming field cannot be empty")
        }
    }

    fun clearState() {
        _registrationState.value = AuthenticationState.Empty
    }
}