package com.example.todoapp.ui.fragment.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentSettingsBinding
import com.example.todoapp.ui.fragment.security.dialogs.CustomConfirmationDialog
import com.example.todoapp.ui.fragment.settings.biometric.BiometricListener
import com.example.todoapp.ui.fragment.settings.biometric.BiometricManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(), BiometricListener {

    private var binding: FragmentSettingsBinding? = null

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.buttonSecurity?.setOnClickListener {
            lifecycleScope.launch {
                if (settingsViewModel.getSecureStatus() == 1) {
                    biometricVerify()
                } else {
                    findNavController().navigate(R.id.navigate_settingsFragment_to_securityFragment)
                }
            }
        }
    }

    private fun biometricVerify() {
        if (BiometricManager.isBiometricSupported(requireContext())) {
            if (BiometricManager.isBiometricEnrolled(requireContext())) {
                BiometricManager.showBiometricPrompt(
                    activity = requireActivity(),
                    listener = this,
                    cryptoObject = null
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "Biometric authentication is available but not set up!",
                    Toast.LENGTH_LONG
                ).show()
                showSetupDialog()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "No biometric features available on this device",
                Toast.LENGTH_SHORT
            ).show()
            showSetupDialog()
        }
    }

    private fun showSetupDialog() {
        CustomConfirmationDialog.newInstance(
            title = "Setup Security",
            message = "Biometric authentication is not set up. Do you want to configure a PIN, pattern, or password?",
            onYes = {
                BiometricManager.promptDeviceCredentialSetup(requireActivity())
            },
        ).show(parentFragmentManager, "SetupSecurityDialog")
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(requireContext(), "Biometric Error: $errMsg", Toast.LENGTH_LONG).show()
        if (isAdded && findNavController().currentDestination?.id == R.id.settingsFragment) {
            findNavController().popBackStack()
        }
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        if (isAdded && findNavController().currentDestination?.id == R.id.settingsFragment) {
            findNavController().navigate(R.id.navigate_settingsFragment_to_securityFragment)
        }
    }

    override fun onBiometricAuthenticateFailed(failedMsg: String) {
        Toast.makeText(requireContext(), "Biometric Failed: $failedMsg", Toast.LENGTH_LONG).show()
    }

}