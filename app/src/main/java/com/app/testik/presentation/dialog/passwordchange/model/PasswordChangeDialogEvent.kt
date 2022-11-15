package com.app.testik.presentation.dialog.passwordchange.model


sealed class PasswordChangeDialogEvent {

    data class ShowSnackbar(val message: String) : PasswordChangeDialogEvent()

    object Loading : PasswordChangeDialogEvent()

    object PasswordChanged : PasswordChangeDialogEvent()
}