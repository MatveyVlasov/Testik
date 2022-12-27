package com.app.testik.presentation.dialog.testinfo.model

import androidx.annotation.StringRes
import com.app.testik.domain.model.TestPassedModel

sealed class TestInfoDialogEvent {

    data class ShowSnackbar(val message: String) : TestInfoDialogEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestInfoDialogEvent()

    object Loading : TestInfoDialogEvent()

    data class SuccessTestCreation(val test: TestPassedModel) : TestInfoDialogEvent()
}