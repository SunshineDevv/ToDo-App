package com.example.todoapp.ui.fragment.auth.recoverpass

import android.os.Bundle
import android.view.Gravity
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
import androidx.room.util.findColumnIndexBySuffix
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentForgetPassBinding
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.google.android.material.snackbar.Snackbar
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
        lifecycleScope.launch {
            forgetPassViewModel.resetState.flowWithLifecycle(lifecycle).collectLatest { state ->
                when (state) {

                    is AuthenticationState.SuccessReset -> {
                        Toast.makeText(requireContext(), state.successMsg, Toast.LENGTH_LONG).apply {
                                show()
                            }
                        findNavController().navigate(R.id.navigate_forgetPassFragment_to_logInFragment)
                        forgetPassViewModel.clearState()
                    }

                    is AuthenticationState.ErrorReset -> {
                        Toast.makeText(requireContext(), state.errorMsg, Toast.LENGTH_LONG).apply {
                            show()
                        }
                        forgetPassViewModel.clearState()
                    }

                    else -> {}
                }
            }
        }
    }
}