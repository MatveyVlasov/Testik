package com.app.tests.presentation.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tests.domain.model.RegistrationModel
import com.app.tests.domain.model.onError
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.usecase.LoginWithEmailUseCase
import com.app.tests.domain.usecase.LoginWithGoogleUseCase
import com.app.tests.presentation.model.UIState
import com.app.tests.presentation.screen.login.model.LoginScreenEvent
import com.app.tests.presentation.screen.login.model.LoginScreenUIState
import com.app.tests.presentation.screen.login.mapper.toDomain
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<LoginScreenUIState>>
        get() = _uiState

    val event: SharedFlow<LoginScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<LoginScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<LoginScreenEvent>()

    private var screenUIState = LoginScreenUIState()

    fun onEmailChanged(email: String) = updateScreenState(screenUIState.copy(email = email))

    fun onPasswordChanged(password: String) = updateScreenState(screenUIState.copy(password = password))

    fun login() {
        emitEvent(LoginScreenEvent.Loading)

        viewModelScope.launch {
            loginWithEmailUseCase(screenUIState.toDomain()).onSuccess {
                emitEvent(LoginScreenEvent.SuccessLogin)
            }.onError {
                emitEvent(LoginScreenEvent.ShowSnackbar(it))
            }
        }
    }

    fun loginWithGoogle(credential: AuthCredential, email: String, username: String) {
        emitEvent(LoginScreenEvent.Loading)

        viewModelScope.launch {
            loginWithGoogleUseCase(credential, RegistrationModel(email = email, username = username)).onSuccess {
                emitEvent(LoginScreenEvent.SuccessLogin)
            }.onError {
                emitEvent(LoginScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun updateScreenState(state: LoginScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: LoginScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

}