package com.app.tests.presentation.screen.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.tests.databinding.FragmentLoginBinding
import com.app.tests.presentation.base.BaseFragment
import com.app.tests.presentation.model.onSuccess
import com.app.tests.presentation.screen.login.model.LoginScreenEvent
import com.app.tests.presentation.screen.login.model.LoginScreenUIState
import com.app.tests.util.hideKeyboard
import com.app.tests.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater) = FragmentLoginBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }

    private fun initViews() {

        binding.apply {
            btnLogin.setOnClickListener {
                viewModel.login()
                hideKeyboard()
            }

            tvRegister.setOnClickListener {
                navController.navigate(
                    LoginFragmentDirections.toRegistration()
                )
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            etEmail.addTextChangedListener { viewModel.onEmailChanged(it.toString()) }
            etPassword.addTextChangedListener { viewModel.onPasswordChanged(it.toString()) }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess {
                            renderUIState(it)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun renderUIState(data: LoginScreenUIState) {
        binding.apply {
            if (!etEmail.isFocused) etEmail.setText(data.email)
            if (!etPassword.isFocused) etPassword.setText(data.password)

            btnLogin.isEnabled = data.canLogin
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    private fun handleEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.ShowSnackbar -> {
                setLoadingState(false)
                showSnackbar(message = event.message)
            }
            is LoginScreenEvent.Loading -> setLoadingState(true)
            is LoginScreenEvent.SuccessLogin -> {
                showSnackbar(message = "You successfully logged in")
                navController.navigate(LoginFragmentDirections.toMain())
            }
        }
    }
}