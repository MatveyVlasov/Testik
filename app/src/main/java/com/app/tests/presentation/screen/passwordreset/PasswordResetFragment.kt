package com.app.tests.presentation.screen.passwordreset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.tests.databinding.FragmentPasswordResetBinding
import com.app.tests.presentation.base.BaseFragment
import com.app.tests.presentation.model.onSuccess
import com.app.tests.presentation.screen.passwordreset.model.PasswordResetScreenEvent
import com.app.tests.presentation.screen.passwordreset.model.PasswordResetScreenUIState
import com.app.tests.util.getStringOrNull
import com.app.tests.util.hideKeyboard
import com.app.tests.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PasswordResetFragment : BaseFragment<FragmentPasswordResetBinding>() {

    private val viewModel: PasswordResetViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater) = FragmentPasswordResetBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }

    private fun initViews() {

        binding.apply {

        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                navController.navigateUp()
            }

            btnReset.setOnClickListener {
                viewModel.resetPassword()
                hideKeyboard()
            }

            etEmail.addTextChangedListener { viewModel.onEmailChanged(it.toString()) }
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

    private fun renderUIState(data: PasswordResetScreenUIState) {
        binding.apply {
            if (!etEmail.isFocused) etEmail.setText(data.email)

            tilEmail.error = getStringOrNull(data.emailError)

            btnReset.isEnabled = data.canReset
        }
        setLoadingState(false)
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    private fun handleEvent(event: PasswordResetScreenEvent) {
        when (event) {
            is PasswordResetScreenEvent.ShowSnackbar -> {
                setLoadingState(false)
                showSnackbar(message = event.message)
            }
            is PasswordResetScreenEvent.Loading -> setLoadingState(true)
            is PasswordResetScreenEvent.EmailSent -> {
                setLoadingState(false)
                binding.tvEmailSent.isVisible = true
            }
        }
    }
}