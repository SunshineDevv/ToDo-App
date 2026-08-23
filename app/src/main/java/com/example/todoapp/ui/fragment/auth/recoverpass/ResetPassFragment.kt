package com.example.todoapp.ui.fragment.auth.recoverpass

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentResetPassBinding
import com.example.todoapp.ui.activity.ActivityUIController
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPassFragment : Fragment() {

    private var binding: FragmentResetPassBinding? = null

    private val resetPassViewModel: ResetPassViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reset_pass, container, false)
        binding?.viewmodel = resetPassViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resetToken = arguments?.getString(ARG_RESET_TOKEN).orEmpty()

        resetPassViewModel.resetToken.value = resetToken
        binding?.editTextResetToken?.setText(resetToken)

        initObservers()

        binding?.buttonResetPassword?.setOnClickListener {
            val token = binding?.editTextResetToken?.text.toString()
            val newPassword = binding?.editTextNewPassword?.text.toString()
            val confirmPassword = binding?.editTextConfirmPassword?.text.toString()

            resetPassViewModel.resetPassword(
                token = token,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            resetPassViewModel.resetState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { state ->
                    val activityUI = requireActivity() as ActivityUIController

                    when (state) {
                        is AuthenticationState.Loading -> {
                            activityUI.showProgressBar(true)
                        }

                        is AuthenticationState.PasswordResetCompleted -> {
                            activityUI.showProgressBar(false)

                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()

                            findNavController().navigate(
                                R.id.navigate_resetPassFragment_to_logInFragment
                            )

                            resetPassViewModel.clearState()
                        }

                        is AuthenticationState.ErrorReset -> {
                            activityUI.showProgressBar(false)

                            Toast.makeText(
                                requireContext(),
                                state.errorMsg,
                                Toast.LENGTH_LONG
                            ).show()

                            resetPassViewModel.clearState()
                        }

                        else -> {
                            activityUI.showProgressBar(false)
                        }
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private companion object {
        const val ARG_RESET_TOKEN = "resetToken"
    }
}