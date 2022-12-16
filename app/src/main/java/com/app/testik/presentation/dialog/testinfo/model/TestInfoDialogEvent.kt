package com.app.testik.presentation.dialog.testinfo.model

import androidx.annotation.StringRes

sealed class TestInfoDialogEvent {

    data class ShowSnackbar(val message: String) : TestInfoDialogEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestInfoDialogEvent()

    data class SuccessTestCreation(val id: String) : TestInfoDialogEvent()
}