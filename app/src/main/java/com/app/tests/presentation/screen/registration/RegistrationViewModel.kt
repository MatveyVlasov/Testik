package com.app.tests.presentation.screen.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tests.domain.model.onError
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.usecase.CreateUserUseCase
import com.app.tests.presentation.model.UIState
import com.app.tests.presentation.screen.registration.mapper.toDomain
import com.app.tests.presentation.screen.registration.model.RegistrationScreenEvent
import com.app.tests.presentation.screen.registration.model.RegistrationScreenUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<RegistrationScreenUIState>>
        get() = _uiState

    val event: SharedFlow<RegistrationScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<RegistrationScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<RegistrationScreenEvent>()

    private var screenUIState = RegistrationScreenUIState()

    fun onEmailChanged(email: String) = updateScreenState(screenUIState.copy(email = email))

    fun onPasswordChanged(password: String) = updateScreenState(screenUIState.copy(password = password))

    fun onPasswordRepeatedChanged(password: String) = updateScreenState(screenUIState.copy(passwordRepeated = password))

    fun onUsernameChanged(username: String) = updateScreenState(screenUIState.copy(username = username))

    fun createUser() {
        emitEvent(RegistrationScreenEvent.Loading)

        viewModelScope.launch {
            createUserUseCase(screenUIState.toDomain()).onSuccess {
                emitEvent(RegistrationScreenEvent.SuccessRegistration)
            }.onError {
                emitEvent(RegistrationScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun updateScreenState(state: RegistrationScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: RegistrationScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

}