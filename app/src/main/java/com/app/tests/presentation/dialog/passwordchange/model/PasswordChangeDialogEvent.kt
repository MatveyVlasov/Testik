package com.app.tests.presentation.dialog.passwordchange.model


sealed class PasswordChangeDialogEvent {

    data class ShowSnackbar(val message: String) : PasswordChangeDialogEvent()

    object Loading : PasswordChangeDialogEvent()

    object PasswordChanged : PasswordChangeDialogEvent()
}