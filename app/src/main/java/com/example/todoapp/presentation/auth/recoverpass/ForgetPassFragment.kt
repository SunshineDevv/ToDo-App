package com.example.todoapp.presentation.auth.recoverpass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentForgetPassBinding
import com.example.todoapp.presentation.activity.ActivityUIController
import com.example.todoapp.presentation.auth.state.AuthenticationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPassFragment : Fragment() {

    private var binding: FragmentForgetPassBinding? = null

    private val forgetPassViewModel: ForgetPassViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_forget_pass, container, false)
        binding?.viewmodel = forgetPassViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()

        binding?.buttonSendCode?.setOnClickListener {
            val userEmail = binding?.editTextEmail?.text.toString().trim()
            forgetPassViewModel.resetPassword(userEmail)
        }

        binding?.linearLayoutLogIn?.setOnClickListener {
            findNavController().navigate(R.id.navigate_forgetPassFragment_to_logInFragment)
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            forgetPassViewModel.resetState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { state ->
                    val activityUI = requireActivity() as ActivityUIController

                    when (state) {
                        is AuthenticationState.Loading -> {
                            activityUI.showProgressBar(true)
                        }

                        is AuthenticationState.PasswordResetRequested -> {
                            activityUI.showProgressBar(false)

                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()

                            val devResetToken = state.devResetToken

                            if (!devResetToken.isNullOrBlank()) {
                                findNavController().navigate(
                                    R.id.navigate_forgetPassFragment_to_resetPassFragment,
                                    bundleOf(ARG_RESET_TOKEN to devResetToken)
                                )
                            } else {
                                findNavController().navigate(
                                    R.id.navigate_forgetPassFragment_to_logInFragment
                                )
                            }

                            forgetPassViewModel.clearState()
                        }

                        is AuthenticationState.ErrorReset -> {
                            activityUI.showProgressBar(false)

                            Toast.makeText(
                                requireContext(),
                                state.errorMsg,
                                Toast.LENGTH_LONG
                            ).show()

                            forgetPassViewModel.clearState()
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