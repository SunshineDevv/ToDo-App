package com.example.todoapp.presentation.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class BiometricPromptManager private constructor(
    private val fragment: Fragment?,
    private val activity: FragmentActivity?
) {

    constructor(fragment: Fragment) : this(
        fragment = fragment,
        activity = null
    )

    constructor(activity: FragmentActivity) : this(
        fragment = null,
        activity = activity
    )

    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (
            errorCode: Int,
            errorMessage: CharSequence
        ) -> Unit
    ) {

        val context =
            fragment?.requireContext()
                ?: requireNotNull(activity)

        val executor =
            ContextCompat.getMainExecutor(context)

        val callback =
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    // Wrong biometric.
                    // Prompt remains open and another attempt is allowed.
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(
                        errorCode,
                        errString
                    )

                    onError(
                        errorCode,
                        errString
                    )
                }
            }

        val biometricPrompt =
            when {
                fragment != null ->
                    BiometricPrompt(
                        fragment,
                        executor,
                        callback
                    )

                activity != null ->
                    BiometricPrompt(
                        activity,
                        executor,
                        callback
                    )

                else ->
                    error("BiometricPrompt host is unavailable.")
            }

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
                .setNegativeButtonText("Cancel")
                .build()

        biometricPrompt.authenticate(promptInfo)
    }
}