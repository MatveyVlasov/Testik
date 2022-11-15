package com.app.testik.presentation.dialog.passwordchange.model

import androidx.annotation.StringRes


sealed class PasswordChangeDialogEvent {

    data class ShowSnackbar(val message: String) : PasswordChangeDialogEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : PasswordChangeDialogEvent()

    object Loading : PasswordChangeDialogEvent()

    object PasswordChanged : PasswordChangeDialogEvent()
}