package com.app.tests.presentation.screen.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tests.R
import com.app.tests.domain.model.onError
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.usecase.CreateUserUseCase
import com.app.tests.presentation.model.UIState
import com.app.tests.presentation.screen.registration.mapper.toDomain
import com.app.tests.presentation.screen.registration.model.RegistrationScreenEvent
import com.app.tests.presentation.screen.registration.model.RegistrationScreenUIState
import com.app.tests.util.isEmail
import com.app.tests.util.isUsername
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
            val error = if (it) null else R.string.email_badly_formatted
            updateScreenState(screenUIState.copy(emailError = error))
        }
    }

    private fun checkPasswordLength(): Boolean {
        return (screenUIState.password.length >= MIN_PASSWORD_LENGTH).also {
            val error = if (it) null else R.string.password_too_short
            updateScreenState(screenUIState.copy(passwordError = error))
        }
    }

    private fun checkPasswordsMatch(): Boolean {
        return (screenUIState.password == screenUIState.passwordRepeated).also {
            val error = if (it) null else R.string.no_passwords_match
            updateScreenState(screenUIState.copy(passwordRepeatedError = error))
        }
    }

    private fun checkUsername(): Boolean {
        return (screenUIState.username.isUsername()).also {
            val error = if (it) null else R.string.username_badly_formatted
            updateScreenState(screenUIState.copy(usernameError = error))
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

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}