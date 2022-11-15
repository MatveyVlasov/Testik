package com.app.testik.presentation.screen.passwordreset.model


sealed class PasswordResetScreenEvent {

    data class ShowSnackbar(val message: String) : PasswordResetScreenEvent()

    object Loading : PasswordResetScreenEvent()

    object EmailSent : PasswordResetScreenEvent()
}