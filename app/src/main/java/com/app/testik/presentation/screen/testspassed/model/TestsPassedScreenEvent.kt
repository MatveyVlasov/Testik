package com.app.testik.presentation.screen.testspassed.model

import androidx.annotation.StringRes

sealed class TestsPassedScreenEvent {

    data class ShowSnackbar(val message: String) : TestsPassedScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestsPassedScreenEvent()

    object Loading : TestsPassedScreenEvent()
}