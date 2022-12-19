package com.app.testik.presentation.screen.testresults.model

import androidx.annotation.StringRes

sealed class TestResultsScreenEvent {

    data class ShowSnackbar(val message: String) : TestResultsScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestResultsScreenEvent()

    object Loading : TestResultsScreenEvent()
}