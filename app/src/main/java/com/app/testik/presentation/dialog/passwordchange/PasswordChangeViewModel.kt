package com.app.testik.presentation.dialog.passwordchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.ChangePasswordUseCase
import com.app.testik.presentation.dialog.passwordchange.model.PasswordChangeDialogEvent
import com.app.testik.presentation.dialog.passwordchange.model.PasswordChangeDialogUIState
import com.app.testik.presentation.model.UIState
import com.app.testik.util.Constants.MIN_PASSWORD_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordChangeViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<PasswordChangeDialogUIState>>
        get() = _uiState

    val event: SharedFlow<PasswordChangeDialogEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<PasswordChangeDialogUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<PasswordChangeDialogEvent>()

    private var screenUIState = PasswordChangeDialogUIState()

    fun onOldPasswordChanged(oldPassword: String) {
        if (oldPassword == screenUIState.oldPassword) return
        updateScreenState(screenUIState.copy(oldPassword = oldPassword, oldPasswordError = null))
    }

    fun onNewPasswordChanged(newPassword: String) {
        if (newPassword == screenUIState.newPassword) return
        updateScreenState(screenUIState.copy(newPassword = newPassword, newPasswordError = null, passwordRepeatedError = null))
    }

    fun onPasswordRepeatedChanged(passwordRepeated: String) {
        if (passwordRepeated == screenUIState.passwordRepeated) return
        updateScreenState(screenUIState.copy(passwordRepeated = passwordRepeated, passwordRepeatedError = null))
    }

    fun changePassword() {
        if (!validateData()) return
        emitEvent(PasswordChangeDialogEvent.Loading)

        viewModelScope.launch {
            changePasswordUseCase(screenUIState.oldPassword, screenUIState.newPassword).onSuccess {
                emitEvent(PasswordChangeDialogEvent.PasswordChanged)
            }.onError {
                handleError(it)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(PasswordChangeDialogEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("password is invalid") -> {
                updateScreenState(screenUIState.copy(oldPasswordError = R.string.incorrect_password))
            }
            else -> emitEvent(PasswordChangeDialogEvent.ShowSnackbar(error))
        }
    }

    private fun validateData(): Boolean {
        return checkPasswordLength() && checkPasswordsMatch()
    }

    private fun checkPasswordLength(): Boolean {
        return (screenUIState.newPassword.length >= MIN_PASSWORD_LENGTH).also {
            if (!it) updateScreenState(screenUIState.copy(newPasswordError = R.string.password_too_short))
        }
    }

    private fun checkPasswordsMatch(): Boolean {
        return (screenUIState.newPassword == screenUIState.passwordRepeated).also {
            if (!it) updateScreenState(screenUIState.copy(passwordRepeatedError = R.string.no_passwords_match))
        }
    }

    private fun updateScreenState(state: PasswordChangeDialogUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: PasswordChangeDialogEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

}