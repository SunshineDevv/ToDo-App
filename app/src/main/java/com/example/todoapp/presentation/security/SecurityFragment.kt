package com.example.todoapp.presentation.security

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.databinding.FragmentSecurityBinding
import com.example.todoapp.domain.security.service.UnifiedOtpManager
import com.example.todoapp.presentation.security.state.SecurityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@AndroidEntryPoint
class SecurityFragment : Fragment() {

    private var binding: FragmentSecurityBinding? = null

    private val securityViewModel: SecurityViewModel by viewModels()

    @Inject
    lateinit var otpManager: UnifiedOtpManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecurityBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        securityViewModel.onStart()

        setupClickListeners()
        observeMfaStatus()
        observeSecurityState()
    }

    private fun setupClickListeners() {
        binding?.beginMfaSetupButton?.setOnClickListener {
            val password = binding?.currentPasswordEditText?.text.toString()

            securityViewModel.beginMfaSetup(password = password)
        }

        binding?.confirmMfaSetupButton?.setOnClickListener {
            val code = binding?.setupCodeEditText?.text.toString()

            securityViewModel.confirmMfaSetup(code = code)
        }

        binding?.disableMfaButton?.setOnClickListener {
            val password = binding?.disablePasswordEditText?.text.toString()
            val code = binding?.disableCodeEditText?.text.toString()

            securityViewModel.disableMfa(
                password = password,
                code = code
            )
        }
    }

    private fun observeMfaStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            securityViewModel.isMfaEnabled
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { isEnabled ->
                    binding?.mfaStatusTextView?.text = if (isEnabled) {
                        "MFA status: enabled"
                    } else {
                        "MFA status: disabled"
                    }

                    binding?.beginMfaSetupButton?.isEnabled = !isEnabled
                    binding?.disableMfaButton?.isEnabled = isEnabled

                    binding?.disableSectionTitleTextView?.visibility = if (isEnabled) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    binding?.disablePasswordEditText?.visibility = if (isEnabled) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    binding?.disableCodeEditText?.visibility = if (isEnabled) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    binding?.disableMfaButton?.visibility = if (isEnabled) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
        }
    }

    private fun observeSecurityState() {
        viewLifecycleOwner.lifecycleScope.launch {
            securityViewModel.securityState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { securityState ->
                    when (securityState) {
                        is SecurityState.Empty -> {
                            hideProgress()
                        }

                        is SecurityState.Loading -> {
                            showProgress()
                        }

                        is SecurityState.MfaSetupStarted -> {
                            hideProgress()

                            showMfaSetupData(
                                otpUri = securityState.otpUri,
                                secretBase32 = securityState.secretBase32
                            )

                            Toast.makeText(
                                requireContext(),
                                "Scan QR code or add secret manually, then enter the generated code.",
                                Toast.LENGTH_LONG
                            ).show()

                            securityViewModel.clearState()
                        }

                        is SecurityState.Success -> {
                            hideProgress()
                            clearInputFields()

                            Toast.makeText(
                                requireContext(),
                                securityState.successMsg,
                                Toast.LENGTH_SHORT
                            ).show()

                            securityViewModel.clearState()
                        }

                        is SecurityState.Error -> {
                            hideProgress()

                            Toast.makeText(
                                requireContext(),
                                securityState.errorMsg,
                                Toast.LENGTH_LONG
                            ).show()

                            securityViewModel.clearState()
                        }
                    }
                }
        }
    }

    private fun showMfaSetupData(
        otpUri: String,
        secretBase32: String
    ) {
        binding?.setupDataContainer?.visibility = View.VISIBLE

        binding?.base32SecretEditText?.setText(secretBase32)

        binding?.qrCodeImageView?.setImageBitmap(
            otpManager.generateQrCode(otpUri)
        )

        binding?.qrCodeImageView?.setOnClickListener {
            openAuthenticatorApp(otpUri)
        }

        binding?.clickQrCodeTextView?.visibility = View.VISIBLE
    }

    private fun clearInputFields() {
        binding?.currentPasswordEditText?.setText("")
        binding?.setupCodeEditText?.setText("")
        binding?.disablePasswordEditText?.setText("")
        binding?.disableCodeEditText?.setText("")
    }

    private fun showProgress() {
        binding?.progressIndicator?.visibility = View.VISIBLE
        binding?.dimOverlay?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding?.progressIndicator?.visibility = View.GONE
        binding?.dimOverlay?.visibility = View.GONE
    }

    private fun openAuthenticatorApp(otpUri: String) {
        try {
            Log.e("TestQr","Uri = $otpUri")
            val intent = Intent(Intent.ACTION_VIEW, otpUri.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Unable to open authenticator app.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}