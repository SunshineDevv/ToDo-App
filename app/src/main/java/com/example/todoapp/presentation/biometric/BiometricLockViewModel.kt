package com.example.todoapp.presentation.biometric

import androidx.lifecycle.ViewModel
import com.example.todoapp.domain.biometric.repository.BiometricLockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class BiometricLockUiState(
    val enabled: Boolean = false
)

@HiltViewModel
class BiometricLockViewModel @Inject constructor(
    private val biometricLockRepository:
    BiometricLockRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            BiometricLockUiState(
                enabled =
                    biometricLockRepository.isEnabled()
            )
        )

    val state =
        _state.asStateFlow()

    fun setEnabled(enabled: Boolean) {

        biometricLockRepository.setEnabled(enabled)

        _state.value =
            BiometricLockUiState(
                enabled = enabled
            )
    }
}