package com.app.testik.presentation.dialog.passwordchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.databinding.FragmentPasswordChangeBinding
import com.app.testik.presentation.base.BaseBottomSheetDialogFragment
import com.app.testik.presentation.dialog.passwordchange.model.PasswordChangeDialogEvent
import com.app.testik.presentation.dialog.passwordchange.model.PasswordChangeDialogUIState
import com.app.testik.presentation.model.onSuccess
import com.app.testik.util.Constants.PASSWORD_CHANGED_RESULT_KEY
import com.app.testik.util.getStringOrNull
import com.app.testik.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PasswordChangeFragment : BaseBottomSheetDialogFragment<FragmentPasswordChangeBinding>() {

    private val viewModel: PasswordChangeViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater) = FragmentPasswordChangeBinding.inflate(inflater)

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
            ivClose.setOnClickListener { navController.navigateUp() }
            btnChangePassword.setOnClickListener { viewModel.changePassword() }

            etOldPassword.addTextChangedListener { viewModel.onOldPasswordChanged(it.toString()) }
            etNewPassword.addTextChangedListener { viewModel.onNewPasswordChanged(it.toString()) }
            etPasswordRepeated.addTextChangedListener { viewModel.onPasswordRepeatedChanged(it.toString()) }
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

    private fun renderUIState(data: PasswordChangeDialogUIState) {
        binding.apply {
            if (!etOldPassword.isFocused) etOldPassword.setText(data.oldPassword)
            if (!etNewPassword.isFocused) etNewPassword.setText(data.newPassword)
            if (!etPasswordRepeated.isFocused) etPasswordRepeated.setText(data.passwordRepeated)

            tilOldPassword.error = getStringOrNull(data.oldPasswordError)
            tilNewPassword.error = getStringOrNull(data.newPasswordError)
            tilPasswordRepeated.error = getStringOrNull(data.passwordRepeatedError)

            btnChangePassword.isEnabled = data.canChange
        }
        setLoadingState(false)
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    private fun handleEvent(event: PasswordChangeDialogEvent) {
        when (event) {
            is PasswordChangeDialogEvent.ShowSnackbar -> {
                setLoadingState(false)
                showSnackbar(message = event.message)
            }
            is PasswordChangeDialogEvent.Loading -> setLoadingState(true)
            is PasswordChangeDialogEvent.PasswordChanged -> {
                setResult(PASSWORD_CHANGED_RESULT_KEY, true)
                dismiss()
            }
        }
    }
}