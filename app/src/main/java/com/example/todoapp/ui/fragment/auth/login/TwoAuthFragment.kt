package com.example.todoapp.ui.fragment.auth.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentTwoAuthBinding
import com.example.todoapp.ui.fragment.security.KeystoreHelper
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import org.apache.commons.codec.binary.Base32


class TwoAuthFragment : Fragment() {

    private var binding: FragmentTwoAuthBinding? = null
    private lateinit var otpManager: UnifiedOtpManager

    private var failedAttempts = 0
    private val maxAttempts = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTwoAuthBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedSecret()
        binding?.verifyButton?.setOnClickListener {
            validateUserInputCode()
        }
    }

    private fun loadSavedSecret() {
        val encryptedSecret = SecurePreferencesHelper.getSecret(requireContext())
        if (encryptedSecret != null) {
            try {
                val decryptedSecret = KeystoreHelper.decryptData(encryptedSecret)
                Log.i("SECRET_MY", "$decryptedSecret")
                otpManager = UnifiedOtpManager(Base32().decode(decryptedSecret))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Не вдалося розшифрувати секрет!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateUserInputCode() {
        val userInputCode = binding?.tokenEditText?.text.toString()

        try {
            if (otpManager.validateToken(userInputCode)) {
                Toast.makeText(requireContext(), "✅ Успішний вхід!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.navigate_twoAuthFragment_to_mainActivity)
                requireActivity().finish()
            } else {
                failedAttempts++
                Toast.makeText(requireContext(), "❌ Невірний код! Спроба $failedAttempts з $maxAttempts", Toast.LENGTH_SHORT).show()

                if (failedAttempts >= maxAttempts) {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(requireContext(), "🚫 Ви вичерпали всі спроби! Ви вийшли з акаунту.", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.navigate_twoAuthFragment_to_logInFragment)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "⚠️ Виникла помилка: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("AuthError", "Помилка при вході: ", e)
            FirebaseAuth.getInstance().signOut()
        }
    }
}