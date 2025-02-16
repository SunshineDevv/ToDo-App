package com.example.todoapp.ui.fragment.security

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentSecurityBinding
import com.example.todoapp.extensions.observeFlow
import com.example.todoapp.ui.fragment.security.dialogs.CustomConfirmationDialog
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
            if (securityViewModel.getSecureStatus()) {
                securityViewModel.setSecureDisable()
                setEmptyFields()
                Toast.makeText(
                    requireContext(),
                    "You are disable 2FA successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "You have already disabled 2FA!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startWork() {

        securityViewModel.onStart()

        setupToolbarMenu()

        initObservers()

        lifecycleScope.launch {
            securityViewModel.isSecure.flowWithLifecycle(lifecycle).collectLatest {
                if (it) {
                    binding?.clickQrCodeTextView?.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            securityViewModel.currentAlgorithm.flowWithLifecycle(lifecycle)
                .collectLatest { algorithm ->
                    when (algorithm) {
                        ShaAlgorithm.SHA1.algorithm -> {
                            setVisibleBase32()
                        }

                        ShaAlgorithm.SHA256.algorithm -> {
                            setInvisibleBase32()
                        }

                        ShaAlgorithm.SHA512.algorithm -> {
                            setInvisibleBase32()
                        }

                        else -> {
                            setInvisibleBase32()
                        }
                    }
                }
        }

        observeFlow(securityViewModel.token) {
            binding?.codeEditText?.setText(it)
        }
    }

    private fun setupToolbarMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu_securityfragment, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val selectedAlgorithm = when (menuItem.itemId) {
                    R.id.sha1_algorithm -> ShaAlgorithm.SHA1
                    R.id.sha256_algorithm -> ShaAlgorithm.SHA256
                    R.id.sha512_algorithm -> ShaAlgorithm.SHA512
                    else -> return false
                }

                handleAlgorithmSelection(selectedAlgorithm)
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun handleAlgorithmSelection(algorithm: ShaAlgorithm) {
        if (securityViewModel.getSecureStatus()) {
            CustomConfirmationDialog.newInstance(
                title = "Change algorithm",
                message = "Do you really want to change algorithm?\nIf you click \"Yes\" then you will have to reconnect 2FA with the selected algorithm",
                onYes = {
                    securityViewModel.setSecureDisable()
                    setEmptyFields()
                    securityViewModel.setAlgorithm(algorithm)
                }
            ).show(parentFragmentManager, "CustomDialog")
        } else {
            securityViewModel.setAlgorithm(algorithm)
        }
    }

    private fun setInvisibleBase32() {
        binding?.base32TextView?.visibility = View.GONE
        binding?.base32SecretEditText?.visibility = View.GONE
    }

    private fun setVisibleBase32() {
        binding?.base32TextView?.visibility = View.VISIBLE
        binding?.base32SecretEditText?.visibility = View.VISIBLE
    }

    private fun initObservers() {
        lifecycleScope.launch {
            securityViewModel.securityState.flowWithLifecycle(lifecycle)
                .collectLatest { securityState ->
                    when (securityState) {
                        is SecurityState.Success -> {
                            binding?.progressIndicator?.visibility = View.GONE
                            binding?.dimOverlay?.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                securityState.successMsg,
                                Toast.LENGTH_SHORT
                            ).show()
                            securityViewModel.clearState()
                        }

                        is SecurityState.Error -> {
                            binding?.progressIndicator?.visibility = View.GONE
                            binding?.dimOverlay?.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                securityState.errorMsg,
                                Toast.LENGTH_SHORT
                            ).show()
                            securityViewModel.clearState()
                        }

                        is SecurityState.LoadingData -> {
                            binding?.secretEditText?.setText(securityState.secret)
                            binding?.qrCodeImageView?.setImageBitmap(
                                otpManager.generateQrCode(
                                    securityState.otpUri
                                )
                            )
                            binding?.base32SecretEditText?.setText(securityState.base32secret)
                            binding?.qrCodeImageView?.setOnClickListener {
                                openGoogleAuthenticator(securityState.otpUri)
                            }
                            binding?.progressIndicator?.visibility = View.GONE
                            binding?.dimOverlay?.visibility = View.GONE
                            securityViewModel.clearState()
                        }

                        is SecurityState.Loading -> {
                            binding?.progressIndicator?.visibility = View.VISIBLE
                            binding?.dimOverlay?.visibility = View.VISIBLE
                        }

                        else -> {
                            binding?.progressIndicator?.visibility = View.GONE
                            binding?.dimOverlay?.visibility = View.GONE
                            securityViewModel.clearState()
                        }
                    }
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEmptyFields() {
        binding?.apply {
            codeEditText.setText("")
            secretEditText.setText("")
            qrCodeImageView.setImageDrawable(null)
            base32SecretEditText.setText("")
            binding?.clickQrCodeTextView?.visibility = View.GONE
        }
    }

    private fun openGoogleAuthenticator(otpUri: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(otpUri))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Unable to open Google Authenticator",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}