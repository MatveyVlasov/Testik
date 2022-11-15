package com.app.testik.presentation.screen.passwordreset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.ResetPasswordUseCase
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.passwordreset.model.PasswordResetScreenEvent
import com.app.testik.presentation.screen.passwordreset.model.PasswordResetScreenUIState
import com.app.testik.util.isEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<PasswordResetScreenUIState>>
        get() = _uiState

    val event: SharedFlow<PasswordResetScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<PasswordResetScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<PasswordResetScreenEvent>()

    private var screenUIState = PasswordResetScreenUIState()

    fun onEmailChanged(email: String) {
        if (email == screenUIState.email) return
        updateScreenState(screenUIState.copy(email = email, emailError = null))
    }

    fun resetPassword() {
        if (!validateData()) return
        emitEvent(PasswordResetScreenEvent.Loading)

        viewModelScope.launch {
            resetPasswordUseCase(screenUIState.email).onSuccess {
                emitEvent(PasswordResetScreenEvent.EmailSent)
            }.onError {
                handleError(it)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(PasswordResetScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("badly formatted") -> {
                updateScreenState(screenUIState.copy(emailError = R.string.email_badly_formatted))
            }
            msg.contains("no user record") -> {
                updateScreenState(screenUIState.copy(emailError = R.string.no_user))
            }
            else -> emitEvent(PasswordResetScreenEvent.ShowSnackbar(error))
        }
    }

    private fun validateData(): Boolean {
        return checkEmail()
    }

    private fun checkEmail(): Boolean {
        return (screenUIState.email.isEmail()).also {
            val error = if (it) null else R.string.email_badly_formatted
            updateScreenState(screenUIState.copy(emailError = error))
        }
    }

    private fun updateScreenState(state: PasswordResetScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: PasswordResetScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

}