package com.app.testik.presentation.screen.passwordreset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.databinding.FragmentPasswordResetBinding
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.passwordreset.model.PasswordResetScreenEvent
import com.app.testik.presentation.screen.passwordreset.model.PasswordResetScreenUIState
import com.app.testik.util.getStringOrNull
import com.app.testik.util.hideKeyboard
import com.app.testik.util.showSnackbar
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

    private fun handleEvent(event: PasswordResetScreenEvent) {
        when (event) {
            is PasswordResetScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is PasswordResetScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is PasswordResetScreenEvent.Loading -> Unit
            is PasswordResetScreenEvent.EmailSent -> binding.tvEmailSent.isVisible = true
        }

        setLoadingState(event is PasswordResetScreenEvent.Loading)
    }
}