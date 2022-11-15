package com.app.testik.presentation.screen.passwordreset.model

import androidx.annotation.StringRes


sealed class PasswordResetScreenEvent {

    data class ShowSnackbar(val message: String) : PasswordResetScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : PasswordResetScreenEvent()

    object Loading : PasswordResetScreenEvent()

    object EmailSent : PasswordResetScreenEvent()
}