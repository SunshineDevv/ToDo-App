package com.example.todoapp.ui.fragment.auth.singup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.model.UserDb
import com.example.todoapp.database.repository.UserRepository
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private lateinit var auth: FirebaseAuth

    val userName = MutableStateFlow("")
    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")
    val userConfirmPassword = MutableStateFlow("")

    private val _registrationState =
        MutableStateFlow<AuthenticationState>(AuthenticationState.Empty)
    val registrationState = _registrationState.asStateFlow()

    fun registerNewUser(email: String, password: String, name: String) {
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            val uid = user.uid
                            viewModelScope.launch {
                                try {
                                    repository.upsert(UserDb(userId = uid, userImg = null))
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
    }

    fun clearState() {
        _registrationState.value = AuthenticationState.Empty
    }
}