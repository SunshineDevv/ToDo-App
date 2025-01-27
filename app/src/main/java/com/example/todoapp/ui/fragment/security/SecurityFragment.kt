package com.example.todoapp.ui.fragment.security

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.databinding.FragmentSecurityBinding
import com.example.todoapp.extensions.observeFlow
import com.example.todoapp.ui.fragment.security.securitystate.SecurityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

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

        startWork()

        binding?.generateSecretButton?.setOnClickListener {
            securityViewModel.generateNewSecret()
        }

        binding?.setCustomSecretButton?.setOnClickListener {
            val customSecret = binding?.secretEditText?.text.toString().trim().uppercase()
            securityViewModel.setCustomSecret(customSecret)
        }

        binding?.disableTwoFactorButton?.setOnClickListener {
            if (securityViewModel.isSecure.value == 1) {
                securityViewModel.setSecureDisable()
                setEmptyFields()
                Toast.makeText(requireContext(), "You are disable 2FA successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "You have already disabled 2FA!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startWork() {

        securityViewModel.onStart()

        initObservers()

        lifecycleScope.launch {
            securityViewModel.isSecure.flowWithLifecycle(lifecycle).collectLatest {
                if (it == 1) {
                    KeystoreHelper.generateSecretKey()
                }
            }
        }

        lifecycleScope.launch {
            securityViewModel.securityState.flowWithLifecycle(lifecycle).collectLatest {state ->

                when(state){
                    is SecurityState.LoadingData -> {
                        binding?.secretEditText?.setText(state.secret)
                        binding?.qrCodeImageView?.setImageBitmap(otpManager.generateQrCode(state.otpUri))
                        binding?.base32SecretEditText?.setText(state.base32secret)
                        binding?.qrCodeImageView?.setOnClickListener {
                            openGoogleAuthenticator(state.otpUri)
                        }
                        securityViewModel.clearState()
                    }
                    else -> {}
                }

            }

        }

        observeFlow(securityViewModel.token){
            binding?.codeEditText?.setText(it)
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            securityViewModel.securityState.flowWithLifecycle(lifecycle).collectLatest { securityState ->
                when (securityState) {
                    is SecurityState.Success -> {
                        Toast.makeText(requireContext(), securityState.successMsg, Toast.LENGTH_SHORT).apply {
                            setGravity(Gravity.CENTER, 0, 0)
                            show()
                        }
                        securityViewModel.clearState()
                    }

                    is SecurityState.Error -> {
                        Toast.makeText(requireContext(), securityState.errorMsg, Toast.LENGTH_SHORT).apply {
                            setGravity(Gravity.CENTER, 0, 0)
                            show()
                        }
                        securityViewModel.clearState()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setEmptyFields() {
        binding?.apply {
            codeEditText.setText("")
            secretEditText.setText("")
            qrCodeImageView.setImageDrawable(null)
            base32SecretEditText.setText("")
        }
    }

    private fun openGoogleAuthenticator(otpUri: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(otpUri))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось открыть Google Authenticator", Toast.LENGTH_SHORT).show()
        }
    }


}