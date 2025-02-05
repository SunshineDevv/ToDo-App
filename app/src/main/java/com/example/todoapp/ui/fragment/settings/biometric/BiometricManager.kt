package com.example.todoapp.ui.fragment.settings.biometric

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricManager {

    private const val MAX_ATTEMPTS = 3
    private var failedAttempts = 0

    fun isBiometricSupported(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isBiometricEnrolled(context: Context): Boolean {
        return isBiometricSupported(context)
    }

    private fun createBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

    private fun initializeBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricListener
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticateError(errorCode, errString.toString())
                resetAttempts()

                if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                    Toast.makeText(activity, "Too many failed attempts. Try again later.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticateSuccess(result)
                resetAttempts()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                failedAttempts++

                if (failedAttempts >= MAX_ATTEMPTS) {
                    listener.onBiometricAuthenticateFailed("Too many failed attempts. Authentication canceled.")
                    resetAttempts()
                } else {
                    Toast.makeText(activity, "Authentication failed. Try again (${MAX_ATTEMPTS - failedAttempts} attempts left).", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricListener,
        title: String = "Authentication",
        subtitle: String = "Authenticate using your fingerprint, Face ID, or device credentials.",
        description: String = "Ensure secure access to your account.",
        cryptoObject: BiometricPrompt.CryptoObject? = null
    ) {
        failedAttempts = 0

        val promptInfo = createBiometricPromptInfo(title, subtitle, description)
        val biometricPrompt = initializeBiometricPrompt(activity, listener)

        if (cryptoObject == null) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }

    fun promptDeviceCredentialSetup(activity: FragmentActivity) {
        val biometricManager = BiometricManager.from(activity)

        if (biometricManager.canAuthenticate(DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(activity, "No PIN, password, or pattern set. Please configure a screen lock manually.", Toast.LENGTH_LONG).show()
            activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            return
        }

        showBiometricPrompt(
            activity = activity,
            listener = object : BiometricListener {
                override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
                    Toast.makeText(activity, "Authentication setup canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
                    Toast.makeText(activity, "Screen lock setup complete", Toast.LENGTH_SHORT).show()
                }

                override fun onBiometricAuthenticateFailed(failedMsg: String) {
                    Toast.makeText(activity, "Authentication setup failed", Toast.LENGTH_SHORT).show()
                }
            },
            title = "Setup Screen Lock",
            subtitle = "Set up a PIN, pattern, or password for security."
        )
    }

    private fun resetAttempts() {
        failedAttempts = 0
    }
}
