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
import com.example.todoapp.databinding.FragmentTwoAuthBinding
import com.example.todoapp.ui.fragment.auth.AuthenticationState
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TwoAuthFragment : Fragment() {

    private var binding: FragmentTwoAuthBinding? = null

    private val twoAuthViewModel: TwoAuthVIewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_two_auth, container, false)
        binding?.viewmodel = twoAuthViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()

        binding?.verifyButton?.setOnClickListener {
            val inputToken = binding?.tokenEditText?.text.toString()
            twoAuthViewModel.validateUserInputCode(inputToken)
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            twoAuthViewModel.twoAuthState.flowWithLifecycle(lifecycle).collectLatest { twoAuthState ->
                when (twoAuthState) {
                    is AuthenticationState.Success -> {
                        findNavController().navigate(R.id.navigate_twoAuthFragment_to_mainActivity)
                        requireActivity().finish()
                        binding?.progressIndicator?.visibility = View.GONE
                        twoAuthViewModel.clearState()
                    }

                    is AuthenticationState.FatalError -> {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(requireContext(),twoAuthState.errorMsg, Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.navigate_twoAuthFragment_to_logInFragment)
                        binding?.progressIndicator?.visibility = View.GONE
                        binding?.dimOverlay?.visibility = View.GONE
                        twoAuthViewModel.clearState()
                    }

                    is AuthenticationState.Error -> {
                        Toast.makeText(requireContext(),twoAuthState.errorMsg, Toast.LENGTH_LONG).show()
                        binding?.progressIndicator?.visibility = View.GONE
                        binding?.dimOverlay?.visibility = View.GONE
                        twoAuthViewModel.clearState()
                    }

                    is AuthenticationState.Loading -> {
                        binding?.progressIndicator?.visibility = View.VISIBLE
                        binding?.dimOverlay?.visibility = View.VISIBLE
                    }

                    else -> {
                        binding?.progressIndicator?.visibility = View.GONE
                        binding?.dimOverlay?.visibility = View.GONE
                        twoAuthViewModel.clearState()
                    }
                }
            }
        }
    }
}