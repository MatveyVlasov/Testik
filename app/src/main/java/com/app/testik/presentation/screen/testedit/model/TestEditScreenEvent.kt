package com.app.testik.presentation.screen.testedit.model

import androidx.annotation.StringRes
import com.app.testik.domain.model.TestModel

sealed class TestEditScreenEvent {

    data class ShowSnackbar(val message: String) : TestEditScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestEditScreenEvent()

    object Loading : TestEditScreenEvent()

    object SuccessTestCreation : TestEditScreenEvent()

    data class SuccessTestDeletion(val test: TestModel) : TestEditScreenEvent()
}