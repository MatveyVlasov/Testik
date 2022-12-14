package com.app.testik.presentation.screen.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentRegistrationBinding
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.registration.model.RegistrationScreenEvent
import com.app.testik.presentation.screen.registration.model.RegistrationScreenUIState
import com.app.testik.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrationFragment : BaseFragment<FragmentRegistrationBinding>() {

    private val viewModel: RegistrationViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater) = FragmentRegistrationBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }

    private fun initViews() {
        binding.apply {
            toolbar.setNavigationOnClickListener { navController.navigateUp() }
            tvLogin.setOnClickListener { navController.navigateUp() }
            btnRegister.setOnClickListener { viewModel.createUser() }
        }
    }

    private fun initListeners() {
        binding.apply {
            etEmail.addTextChangedListener { viewModel.onEmailChanged(it.toString()) }
            etPassword.addTextChangedListener { viewModel.onPasswordChanged(it.toString()) }
            etPasswordRepeated.addTextChangedListener { viewModel.onPasswordRepeatedChanged(it.toString()) }
            etUsername.addTextChangedListener { viewModel.onUsernameChanged(it.toString()) }
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

    private fun renderUIState(data: RegistrationScreenUIState) {
        binding.apply {
            if (!etEmail.isFocused) etEmail.setText(data.email)
            if (!etPassword.isFocused) etPassword.setText(data.password)
            if (!etPasswordRepeated.isFocused) etPasswordRepeated.setText(data.passwordRepeated)
            if (!etUsername.isFocused) etUsername.setText(data.username)

            tilEmail.error = getStringOrNull(data.emailError)
            tilPassword.error = getStringOrNull(data.passwordError)
            tilPasswordRepeated.error = getStringOrNull(data.passwordRepeatedError)
            tilUsername.error = getStringOrNull(data.usernameError)

            btnRegister.isEnabled = data.canRegister
        }
        setLoadingState(false)
    }

    private fun handleEvent(event: RegistrationScreenEvent) {
        when (event) {
            is RegistrationScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is RegistrationScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is RegistrationScreenEvent.Loading -> Unit
            is RegistrationScreenEvent.SuccessRegistration -> {
                showSnackbar(message = R.string.register_success)
                navController.navigate(
                    RegistrationFragmentDirections.toMain()
                )
            }
        }

        setLoadingState(event is RegistrationScreenEvent.Loading)
    }
}