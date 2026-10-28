package com.example.todoapp.presentation.biometric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.databinding.FragmentBiometricLockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BiometricLockFragment : Fragment() {

    private var binding:
            FragmentBiometricLockBinding? = null

    private val viewModel:
            BiometricLockViewModel by viewModels()

    private lateinit var promptManager:
            BiometricPromptManager

    private lateinit var availabilityChecker:
            BiometricAvailabilityChecker

    private var currentAvailability:
            BiometricAvailability =
        BiometricAvailability.Unsupported

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentBiometricLockBinding.inflate(
                inflater,
                container,
                false
            )

        return requireNotNull(binding).root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        promptManager =
            BiometricPromptManager(this)

        availabilityChecker =
            BiometricAvailabilityChecker(
                requireContext()
            )

        checkAvailability()

        observeState()

        binding
            ?.biometricActionButton
            ?.setOnClickListener {

                val enabled =
                    viewModel.state.value.enabled

                if (enabled) {
                    requestDisable()
                } else {
                    requestEnable()
                }
            }
    }

    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.state
                .flowWithLifecycle(
                    viewLifecycleOwner.lifecycle
                )
                .collectLatest { state ->

                    renderState(state)
                }
        }
    }

    private fun renderState(
        state: BiometricLockUiState
    ) {

        binding
            ?.biometricStatusTextView
            ?.text =
            if (state.enabled) {
                "Biometric lock: enabled"
            } else {
                "Biometric lock: disabled"
            }

        binding
            ?.biometricActionButton
            ?.text =
            if (state.enabled) {
                "Disable biometric lock"
            } else {
                "Enable biometric lock"
            }

        binding
            ?.biometricActionButton
            ?.isEnabled =
            state.enabled || currentAvailability is BiometricAvailability.Available
    }

    private fun checkAvailability() {

        currentAvailability =
            availabilityChecker.check()

        val message =
            when (currentAvailability) {

                BiometricAvailability.Available ->
                    "Strong biometric authentication is available."

                BiometricAvailability.NoHardware ->
                    "This device has no supported biometric hardware."

                BiometricAvailability.HardwareUnavailable ->
                    "Biometric hardware is currently unavailable."

                BiometricAvailability.NoneEnrolled ->
                    "No strong biometric is enrolled on this device."

                BiometricAvailability.SecurityUpdateRequired ->
                    "A security update is required before biometric authentication can be used."

                BiometricAvailability.Unsupported ->
                    "Strong biometric authentication is not available."
            }

        binding
            ?.biometricAvailabilityTextView
            ?.text =
            message

        renderState(
            viewModel.state.value
        )
    }

    private fun requestEnable() {

        if (
            currentAvailability
                    !is BiometricAvailability.Available
        ) {
            Toast.makeText(
                requireContext(),
                "Strong biometric authentication is unavailable.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        promptManager.authenticate(
            title = "Enable biometric lock",
            subtitle =
                "Confirm your biometric identity to enable app protection.",
            onSuccess = {

                viewModel.setEnabled(true)

                Toast.makeText(
                    requireContext(),
                    "Biometric lock enabled.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onError = { _, errorMessage ->

                Toast.makeText(
                    requireContext(),
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun requestDisable() {

        if (
            currentAvailability
                    !is BiometricAvailability.Available
        ) {
            Toast.makeText(
                requireContext(),
                "Biometric authentication is currently unavailable.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        promptManager.authenticate(
            title = "Disable biometric lock",
            subtitle =
                "Confirm your biometric identity to disable app protection.",
            onSuccess = {

                viewModel.setEnabled(false)

                Toast.makeText(
                    requireContext(),
                    "Biometric lock disabled.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onError = { _, errorMessage ->

                Toast.makeText(
                    requireContext(),
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    override fun onResume() {
        super.onResume()

        if (::availabilityChecker.isInitialized) {
            checkAvailability()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }
}