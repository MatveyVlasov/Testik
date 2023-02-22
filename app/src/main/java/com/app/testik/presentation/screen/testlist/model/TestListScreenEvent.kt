package com.app.testik.presentation.screen.testlist.model

import androidx.annotation.StringRes

sealed class TestListScreenEvent {

    data class ShowSnackbar(val message: String) : TestListScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestListScreenEvent()

    object Loading : TestListScreenEvent()
}