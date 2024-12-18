package com.example.todoapp.ui.fragment.auth.login

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
import com.example.todoapp.databinding.FragmentLogInBinding
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogInFragment : Fragment() {

    private var binding: FragmentLogInBinding? = null

    private val logInViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_log_in, container, false)
        binding?.viewmodel = logInViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()

        binding?.logInButton?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString().trim()
            val password = binding?.passwordEditText?.text.toString().trim()
            logInViewModel.logInUser(email, password)
        }

        binding?.signUpTextView?.setOnClickListener {
            findNavController().navigate(R.id.navigate_logInFragment_to_signUpFragment)
        }

    }

    private fun initObservers() {
        lifecycleScope.launch {
            logInViewModel.logInState.flowWithLifecycle(lifecycle).collectLatest { logInState ->
                when (logInState) {
                    is AuthenticationState.Success -> {
                        findNavController().navigate(R.id.navigate_logInFragment_to_mainActivity)
                        requireActivity().finish()
                        logInViewModel.clearState()
                    }

                    is AuthenticationState.Error -> {
                        view?.let {
                            Snackbar.make(it, logInState.errorMsg, Snackbar.LENGTH_SHORT)
                                .setAction("OK"){}
                                .show()
                        }
                        logInViewModel.clearState()
                    }

                    else -> {}
                }
            }
        }
    }
}