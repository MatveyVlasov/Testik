package com.app.testik.presentation.screen.login.model

import androidx.annotation.StringRes

sealed class LoginScreenEvent {

    data class ShowSnackbar(val message: String) : LoginScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : LoginScreenEvent()

    object Loading : LoginScreenEvent()

    object NavigateToMain : LoginScreenEvent()

    object Restart : LoginScreenEvent()
}