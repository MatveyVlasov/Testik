package com.app.testik.presentation.screen.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.CreateUserUseCase
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.registration.mapper.toDomain
import com.app.testik.presentation.screen.registration.model.RegistrationScreenEvent
import com.app.testik.presentation.screen.registration.model.RegistrationScreenUIState
import com.app.testik.util.Constants.MIN_PASSWORD_LENGTH
import com.app.testik.util.isEmail
import com.app.testik.util.isUsername
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

    fun onEmailChanged(email: String) {
        if (email == screenUIState.email) return
        updateScreenState(screenUIState.copy(email = email, emailError = null))
    }

    fun onPasswordChanged(password: String) {
        if (password == screenUIState.password) return
        updateScreenState(screenUIState.copy(password = password, passwordError = null, passwordRepeatedError = null))
    }

    fun onPasswordRepeatedChanged(passwordRepeated: String) {
        if (passwordRepeated == screenUIState.passwordRepeated) return
        updateScreenState(screenUIState.copy(passwordRepeated = passwordRepeated, passwordRepeatedError = null))
    }

    fun onUsernameChanged(username: String) {
        if (username == screenUIState.username) return
        updateScreenState(screenUIState.copy(username = username, usernameError = null))
    }

    fun createUser() {
        if (!validateData()) return
        emitEvent(RegistrationScreenEvent.Loading)

        viewModelScope.launch {
            createUserUseCase(screenUIState.toDomain()).onSuccess {
                emitEvent(RegistrationScreenEvent.SuccessRegistration)
            }.onError {
                handleError(it)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(RegistrationScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("email address is already in use") -> {
                updateScreenState(screenUIState.copy(emailError = R.string.email_already_taken))
            }
            msg.contains("username already taken") -> {
                updateScreenState(screenUIState.copy(usernameError = R.string.username_already_taken))
            }
            else -> emitEvent(RegistrationScreenEvent.ShowSnackbar(error))
        }
    }

    private fun validateData(): Boolean {
        return checkEmail() && checkPasswordLength() && checkPasswordsMatch() && checkUsername()
    }

    private fun checkEmail(): Boolean {
        return (screenUIState.email.isEmail()).also {
            if (!it) updateScreenState(screenUIState.copy(emailError = R.string.email_badly_formatted))
        }
    }

    private fun checkPasswordLength(): Boolean {
        return (screenUIState.password.length >= MIN_PASSWORD_LENGTH).also {
            if (!it) updateScreenState(screenUIState.copy(passwordError = R.string.password_too_short))
        }
    }

    private fun checkPasswordsMatch(): Boolean {
        return (screenUIState.password == screenUIState.passwordRepeated).also {
            if (!it) updateScreenState(screenUIState.copy(passwordRepeatedError = R.string.no_passwords_match))
        }
    }

    private fun checkUsername(): Boolean {
        return (screenUIState.username.isUsername()).also {
            if (!it) updateScreenState(screenUIState.copy(usernameError = R.string.username_badly_formatted))
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