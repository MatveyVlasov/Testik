package com.app.testik.presentation.screen.testedit.model

import androidx.annotation.StringRes

sealed class TestEditScreenEvent {

    data class ShowSnackbar(val message: String) : TestEditScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestEditScreenEvent()

    object Loading : TestEditScreenEvent()

    object SuccessTestCreation : TestEditScreenEvent()
}