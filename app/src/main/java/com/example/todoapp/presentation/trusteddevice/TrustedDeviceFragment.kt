package com.example.todoapp.presentation.trusteddevice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentTrustedDeviceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.todoapp.presentation.activity.AuthActivity

@AndroidEntryPoint
class TrustedDeviceFragment : Fragment() {

    private var binding:
            FragmentTrustedDeviceBinding? = null

    private val viewModel:
            TrustedDeviceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentTrustedDeviceBinding.inflate(
                inflater,
                container,
                false
            )

        return requireNotNull(
            binding
        ).root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupClicks()
        observeState()

        viewModel.loadState()
    }

    private fun setupClicks() {

        binding
            ?.buttonRegisterDevice
            ?.setOnClickListener {

                val password =
                    binding
                        ?.passwordEditText
                        ?.text
                        ?.toString()
                        .orEmpty()

                val totp =
                    binding
                        ?.totpEditText
                        ?.text
                        ?.toString()

                viewModel.enroll(
                    password =
                        password,
                    currentTotp =
                        totp
                )
            }

        binding
            ?.buttonBindSession
            ?.setOnClickListener {

                viewModel
                    .bindCurrentSession()
            }

        binding
            ?.buttonRevokeDevice
            ?.setOnClickListener {

                showRevokeConfirmation()
            }
    }

    private fun observeState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel
                            .state
                            .collect { state ->

                                renderState(
                                    state
                                )
                            }
                    }
            }
    }

    private fun renderState(
        state: TrustedDeviceUiState
    ) {
        val currentBinding =
            binding ?: return

        if (state.requireLogin) {
            openLogin()
            return
        }

        currentBinding.progressBar.visibility =
            if (state.loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        currentBinding.contentContainer.alpha =
            if (state.loading) {
                0.55f
            } else {
                1f
            }

        /*
         * STATE 1:
         * устройство ещё не зарегистрировано.
         */
        if (!state.enrolled) {

            currentBinding
                .statusTitleTextView
                .text =
                "Device not registered"

            currentBinding
                .statusDescriptionTextView
                .text =
                "Register this phone to protect selected sessions with a cryptographic device key."

            currentBinding
                .statusIndicator
                .setBackgroundResource(
                    R.drawable.bg_status_inactive
                )

            currentBinding
                .credentialsContainer
                .visibility =
                View.VISIBLE

            currentBinding
                .buttonRegisterDevice
                .visibility =
                View.VISIBLE

            currentBinding
                .buttonBindSession
                .visibility =
                View.GONE

            currentBinding
                .buttonRevokeDevice
                .visibility =
                View.GONE

            currentBinding
                .deviceIdContainer
                .visibility =
                View.GONE
        }

        else if (!state.bound) {

            currentBinding
                .statusTitleTextView
                .text =
                "Trusted device registered"

            currentBinding
                .statusDescriptionTextView
                .text =
                "This phone has a registered cryptographic key. Bind the current session to enable device-protected refresh."

            currentBinding
                .statusIndicator
                .setBackgroundResource(
                    R.drawable.bg_status_warning
                )

            currentBinding
                .credentialsContainer
                .visibility =
                View.GONE

            currentBinding
                .buttonRegisterDevice
                .visibility =
                View.GONE

            currentBinding
                .buttonBindSession
                .visibility =
                View.VISIBLE

            currentBinding
                .buttonRevokeDevice
                .visibility =
                View.VISIBLE

            currentBinding
                .deviceIdContainer
                .visibility =
                View.VISIBLE
        }

        else {

            currentBinding
                .statusTitleTextView
                .text =
                "Current session protected"

            currentBinding
                .statusDescriptionTextView
                .text =
                "Refresh of this session requires a valid signature from the private key stored on this device."

            currentBinding
                .statusIndicator
                .setBackgroundResource(
                    R.drawable.bg_status_active
                )

            currentBinding
                .credentialsContainer
                .visibility =
                View.GONE

            currentBinding
                .buttonRegisterDevice
                .visibility =
                View.GONE

            currentBinding
                .buttonBindSession
                .visibility =
                View.GONE

            currentBinding
                .buttonRevokeDevice
                .visibility =
                View.VISIBLE

            currentBinding
                .deviceIdContainer
                .visibility =
                View.VISIBLE
        }

        currentBinding
            .deviceIdTextView
            .text =
            state.deviceId.orEmpty()

        currentBinding
            .buttonRegisterDevice
            .isEnabled =
            !state.loading

        currentBinding
            .buttonBindSession
            .isEnabled =
            !state.loading

        currentBinding
            .buttonRevokeDevice
            .isEnabled =
            !state.loading

        renderMessage(
            state
        )
    }

    private fun renderMessage(
        state: TrustedDeviceUiState
    ) {
        val currentBinding =
            binding ?: return

        val message =
            state.message

        if (message.isNullOrBlank()) {

            currentBinding
                .messageTextView
                .visibility =
                View.GONE

            return
        }

        currentBinding
            .messageTextView
            .visibility =
            View.VISIBLE

        currentBinding
            .messageTextView
            .text =
            message

        val color =
            if (state.isError) {
                android.R.color.holo_red_light
            } else {
                R.color.yellow
            }

        currentBinding
            .messageTextView
            .setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    color
                )
            )
    }

    private fun showRevokeConfirmation() {

        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_revoke_trusted_device,
                null
            )

        val dialog =
            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setView(dialogView)
                .create()

        dialog.setOnShowListener {

            dialog.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(
                    android.graphics.Color.TRANSPARENT
                )
            )

            val width =
                (resources.displayMetrics.widthPixels * 0.88f)
                    .toInt()

            dialog.window?.setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogView
            .findViewById<View>(
                R.id.buttonCancel
            )
            .setOnClickListener {

                dialog.dismiss()
            }

        dialogView
            .findViewById<View>(
                R.id.buttonConfirmRevoke
            )
            .setOnClickListener {

                dialog.dismiss()

                viewModel.revokeTrustedDevice()
            }

        dialog.show()
    }

    private fun openLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)

        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }
}