package com.example.todoapp.ui.fragment.settings

import androidx.lifecycle.ViewModel
import com.example.todoapp.database.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getSecureStatus(): Int {
        return userId?.let { repository.isUserSecure(it) } ?: 0
    }
}