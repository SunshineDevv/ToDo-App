package com.example.todoapp.presentation.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

@AndroidEntryPoint
class SecurityFragment : Fragment() {

    private var binding: FragmentSecurityBinding? = null

    private val securityViewModel: SecurityViewModel by viewModels()

    private var currentMfaStatus: Boolean? = null
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

        setupInitialUi()
        setupClickListeners()
        observeMfaStatus()
        observeSecurityState()

        securityViewModel.onStart()
    }

    private fun setupInitialUi() {
        binding?.sha256RadioButton?.isChecked = true

        hideMfaSetupData()
        updateMfaStatusUi(null)
    }

    private fun setupClickListeners() {
        binding?.algorithmRadioGroup?.setOnCheckedChangeListener { _, checkedId ->
            val algorithm = when (checkedId) {
                binding?.sha1RadioButton?.id -> "SHA1"
                binding?.sha256RadioButton?.id -> "SHA256"
                binding?.sha512RadioButton?.id -> "SHA512"
                else -> "SHA256"
            }

            securityViewModel.setAlgorithm(algorithm)
        }

        binding?.beginMfaSetupButton?.setOnClickListener {
            val password = binding?.currentPasswordEditText?.text.toString()
            val currentMfaCode = binding?.currentMfaCodeEditText?.text.toString()

            securityViewModel.beginMfaSetup(
                password = password,
                currentCode = currentMfaCode
            )
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
                    updateMfaStatusUi(isEnabled)
                }
        }
    }

    private fun updateMfaStatusUi(isEnabled: Boolean?) {
        currentMfaStatus = isEnabled

        when (isEnabled) {
            null -> {
                binding?.mfaStatusTextView?.text = "Checking MFA status..."

                setSetupSectionVisible(false)
                setCurrentMfaCodeVisible(false)
                setDisableSectionVisible(false)
                hideMfaSetupData()

                showProgress()
            }

            false -> {
                hideProgress()

                binding?.mfaStatusTextView?.text = "MFA status: disabled"

                binding?.setupSectionTitleTextView?.text = "Enable MFA"
                binding?.beginMfaSetupButton?.text = "START MFA SETUP"

                setSetupSectionVisible(true)
                setCurrentMfaCodeVisible(false)
                setDisableSectionVisible(false)
            }

            true -> {
                hideProgress()

                binding?.mfaStatusTextView?.text = "MFA status: enabled"

                binding?.setupSectionTitleTextView?.text = "Replace MFA algorithm"
                binding?.beginMfaSetupButton?.text = "REPLACE MFA SETUP"

                setSetupSectionVisible(true)
                setCurrentMfaCodeVisible(true)
                setDisableSectionVisible(true)
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
                            if (currentMfaStatus != null) {
                                hideProgress()
                            }
                        }

                        is SecurityState.Loading -> {
                            showProgress()
                        }

                        is SecurityState.MfaSetupStarted -> {
                            hideProgress()

                            showMfaSetupData(
                                otpUri = securityState.otpUri,
                                secretBase32 = securityState.secretBase32,
                                algorithm = securityState.algorithm,
                                digits = securityState.digits,
                                periodSeconds = securityState.periodSeconds
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
                            hideMfaSetupData()

                            Toast.makeText(
                                requireContext(),
                                securityState.successMsg,
                                Toast.LENGTH_SHORT
                            ).show()

                            securityViewModel.onStart()
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

    private fun setSetupSectionVisible(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE

        binding?.setupSectionTitleTextView?.visibility = visibility
        binding?.currentPasswordEditText?.visibility = visibility
        binding?.algorithmTitleTextView?.visibility = visibility
        binding?.algorithmRadioGroup?.visibility = visibility
        binding?.beginMfaSetupButton?.visibility = visibility
    }

    private fun setCurrentMfaCodeVisible(isVisible: Boolean) {
        binding?.currentMfaCodeEditText?.visibility =
            if (isVisible) View.VISIBLE else View.GONE
    }

    private fun setDisableSectionVisible(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE

        binding?.disableSectionTitleTextView?.visibility = visibility
        binding?.disablePasswordEditText?.visibility = visibility
        binding?.disableCodeEditText?.visibility = visibility
        binding?.disableMfaButton?.visibility = visibility
    }

    private fun showMfaSetupData(
        otpUri: String,
        secretBase32: String,
        algorithm: String,
        digits: Int,
        periodSeconds: Int
    ) {
        val normalizedOtpUri = otpUri.trim()
        val cleanSecret = normalizeBase32Secret(secretBase32)

        binding?.setupDataContainer?.visibility = View.VISIBLE

        binding?.base32SecretEditText?.setText(cleanSecret)

        binding?.setupParamsTextView?.text =
            "Algorithm: $algorithm | Digits: $digits | Period: ${periodSeconds}s"

        binding?.copySecretButton?.setOnClickListener {
            copySecretToClipboard(cleanSecret)
        }

        binding?.qrCodeImageView?.setImageBitmap(
            otpManager.generateQrCode(normalizedOtpUri)
        )

        binding?.qrCodeImageView?.setOnClickListener {
            openAuthenticatorApp(normalizedOtpUri)
        }

        binding?.openAuthenticatorButton?.setOnClickListener {
            openAuthenticatorApp(normalizedOtpUri)
        }

        binding?.clickQrCodeTextView?.visibility = View.VISIBLE
    }

    private fun hideMfaSetupData() {
        binding?.setupDataContainer?.visibility = View.GONE
        binding?.base32SecretEditText?.setText("")
        binding?.setupParamsTextView?.text = ""
        binding?.setupCodeEditText?.setText("")
        binding?.qrCodeImageView?.setImageDrawable(null)
        binding?.clickQrCodeTextView?.visibility = View.GONE
    }

    private fun clearInputFields() {
        binding?.currentPasswordEditText?.setText("")
        binding?.currentMfaCodeEditText?.setText("")
        binding?.setupCodeEditText?.setText("")
        binding?.disablePasswordEditText?.setText("")
        binding?.disableCodeEditText?.setText("")
    }

    private fun normalizeBase32Secret(secret: String): String {
        return secret
            .filterNot { it.isWhitespace() }
            .uppercase()
    }

    private fun copySecretToClipboard(secret: String) {
        val cleanSecret = normalizeBase32Secret(secret)

        val clipboard = requireContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText("MFA secret", cleanSecret)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            requireContext(),
            "Secret copied.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openAuthenticatorApp(otpUri: String) {
        val normalizedOtpUri = otpUri.trim()

        if (normalizedOtpUri.isBlank()) {
            Toast.makeText(
                requireContext(),
                "MFA setup URI is empty.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uri = Uri.parse(normalizedOtpUri)

        val googleAuthenticatorIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(GOOGLE_AUTHENTICATOR_PACKAGE)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        try {
            startActivity(googleAuthenticatorIntent)
            return
        } catch (e: Exception) {
            // fallback below
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        try {
            startActivity(Intent.createChooser(fallbackIntent, "Open authenticator app"))
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Unable to open authenticator app. Copy the secret manually.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showProgress() {
        binding?.progressIndicator?.visibility = View.VISIBLE
        binding?.dimOverlay?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding?.progressIndicator?.visibility = View.GONE
        binding?.dimOverlay?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private companion object {
        const val GOOGLE_AUTHENTICATOR_PACKAGE = "com.google.android.apps.authenticator2"
    }
}