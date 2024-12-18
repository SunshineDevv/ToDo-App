package com.example.todoapp.ui.fragment.auth.singup

import android.os.Bundle
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
import com.example.todoapp.databinding.FragmentSignUpBinding
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null

    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        binding?.viewmodel = signUpViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()

        binding?.signUpButton?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString().trim()
            val password = binding?.confirmPasswordEditText?.text.toString().trim()
            val userName = binding?.nameEditText?.text.toString().trim()

            signUpViewModel.registerNewUser(email, password, userName)
        }

        binding?.logInTextView?.setOnClickListener {
            findNavController().navigate(R.id.navigate_signUpFragment_to_logInFragment)
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            signUpViewModel.registrationState.flowWithLifecycle(lifecycle)
                .collectLatest { registrationState ->
                    when (registrationState) {
                        is AuthenticationState.Success -> {
                            findNavController().navigate(R.id.navigate_signUpFragment_to_mainActivity)
                            requireActivity().finish()
                            signUpViewModel.clearState()
                        }

                        is AuthenticationState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                registrationState.errorMsg,
                                Toast.LENGTH_LONG
                            ).show()
                            signUpViewModel.clearState()
                        }

                        else -> {}
                    }
                }
        }
    }
}