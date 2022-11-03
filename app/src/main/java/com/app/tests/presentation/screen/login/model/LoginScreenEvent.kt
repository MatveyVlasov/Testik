package com.app.tests.presentation.screen.login.model

sealed class LoginScreenEvent {

    data class ShowSnackbar(val message: String) : LoginScreenEvent()

    object Loading : LoginScreenEvent()

    object SuccessLogin : LoginScreenEvent()
}