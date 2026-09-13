package com.example.todoapp.presentation.auth.login

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
import com.example.todoapp.presentation.auth.state.AuthenticationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TwoAuthFragment : Fragment() {

    private var binding: FragmentTwoAuthBinding? = null

    private val twoAuthViewModel: TwoAuthVIewModel by viewModels()

    private val loginTicket: String
        get() = requireArguments().getString(ARG_LOGIN_TICKET).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_two_auth,
            container,
            false
        )

        binding?.viewmodel = twoAuthViewModel
        binding?.lifecycleOwner = viewLifecycleOwner

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding?.verifyButton?.setOnClickListener {
            val inputCode = binding?.tokenEditText?.text.toString()

            twoAuthViewModel.validateUserInputCode(
                loginTicket = loginTicket,
                userInputCode = inputCode
            )
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            twoAuthViewModel.twoAuthState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { twoAuthState ->
                    when (twoAuthState) {
                        is AuthenticationState.Success -> {
                            hideProgress()

                            findNavController().navigate(
                                R.id.navigate_twoAuthFragment_to_mainActivity
                            )

                            requireActivity().finish()
                            twoAuthViewModel.clearState()
                        }

                        is AuthenticationState.FatalError -> {
                            hideProgress()

                            Toast.makeText(
                                requireContext(),
                                twoAuthState.errorMsg,
                                Toast.LENGTH_LONG
                            ).show()

                            findNavController().navigate(
                                R.id.navigate_twoAuthFragment_to_logInFragment
                            )

                            twoAuthViewModel.clearState()
                        }

                        is AuthenticationState.Error -> {
                            hideProgress()

                            Toast.makeText(
                                requireContext(),
                                twoAuthState.errorMsg,
                                Toast.LENGTH_LONG
                            ).show()

                            twoAuthViewModel.clearState()
                        }

                        is AuthenticationState.Loading -> {
                            showProgress()
                        }

                        is AuthenticationState.Empty -> {
                            hideProgress()
                        }

                        else -> {
                            hideProgress()
                        }
                    }
                }
        }
    }

    private fun showProgress() {
        binding?.progressIndicator?.visibility = View.VISIBLE
        binding?.dimOverlay?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding?.progressIndicator?.visibility = View.GONE
        binding?.dimOverlay?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private companion object {
        const val ARG_LOGIN_TICKET = "loginTicket"
    }
}