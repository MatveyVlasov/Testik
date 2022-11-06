package com.app.tests.presentation.screen.passwordreset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tests.domain.model.onError
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.usecase.ResetPasswordUseCase
import com.app.tests.presentation.model.UIState
import com.app.tests.presentation.screen.passwordreset.model.PasswordResetScreenEvent
import com.app.tests.presentation.screen.passwordreset.model.PasswordResetScreenUIState
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

    fun onEmailChanged(email: String) = updateScreenState(screenUIState.copy(email = email))

    fun resetPassword() {
        emitEvent(PasswordResetScreenEvent.Loading)

        viewModelScope.launch {
            resetPasswordUseCase(screenUIState.email).onSuccess {
                emitEvent(PasswordResetScreenEvent.EmailSent)
            }.onError {
                emitEvent(PasswordResetScreenEvent.ShowSnackbar(it))
            }
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