package com.app.testik.presentation.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.RegistrationModel
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.GetCurrentUserUseCase
import com.app.testik.domain.usecase.LoginWithEmailUseCase
import com.app.testik.domain.usecase.LoginWithGoogleUseCase
import com.app.testik.domain.usecase.PreferencesUseCase
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.login.model.LoginScreenEvent
import com.app.testik.presentation.screen.login.model.LoginScreenUIState
import com.app.testik.presentation.screen.login.mapper.toDomain
import com.app.testik.util.isEmail
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
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val preferencesUseCase: PreferencesUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<LoginScreenUIState>>
        get() = _uiState

    val event: SharedFlow<LoginScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<LoginScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<LoginScreenEvent>()

    private var screenUIState = LoginScreenUIState()

    init {
        getCurrentUser()
    }

    fun onEmailChanged(email: String) {
        if (email == screenUIState.email) return
        updateScreenState(screenUIState.copy(email = email, emailError = null))
    }

    fun onPasswordChanged(password: String) {
        if (password == screenUIState.password) return
        updateScreenState(screenUIState.copy(password = password, passwordError = null))
    }

    fun login() {
        if (!validateData()) return
        emitEvent(LoginScreenEvent.Loading)

        viewModelScope.launch {
            loginWithEmailUseCase(screenUIState.toDomain()).onSuccess {
                emitEvent(LoginScreenEvent.NavigateToMain)
            }.onError {
                handleError(it)
            }
        }
    }

    fun loginWithGoogle(credential: AuthCredential, email: String, username: String, avatar: String) {
        emitEvent(LoginScreenEvent.Loading)

        viewModelScope.launch {
            loginWithGoogleUseCase(
                credential, RegistrationModel(email = email, username = username, avatar = avatar)
            ).onSuccess {
                emitEvent(LoginScreenEvent.NavigateToMain)
            }.onError {
                emitEvent(LoginScreenEvent.ShowSnackbar(it))
            }
        }
    }

    fun setLanguage(lang: String) {
        preferencesUseCase.setLanguage(lang)
        emitEvent(LoginScreenEvent.Restart)
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase()?.let {
                emitEvent(LoginScreenEvent.NavigateToMain)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no user record") -> {
                updateScreenState(screenUIState.copy(emailError = R.string.no_user))
            }
            msg.contains("password is invalid") -> {
                updateScreenState(screenUIState.copy(passwordError = R.string.incorrect_password))
            }
            msg.contains("unusual activity") -> {
                emitEvent(LoginScreenEvent.ShowSnackbarByRes(R.string.too_many_requests))
            }
            else -> emitEvent(LoginScreenEvent.ShowSnackbar(error))
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