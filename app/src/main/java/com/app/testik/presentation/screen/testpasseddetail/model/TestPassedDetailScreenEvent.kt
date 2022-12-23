package com.app.testik.presentation.screen.testpasseddetail.model

import androidx.annotation.StringRes

sealed class TestPassedDetailScreenEvent {

    data class ShowSnackbar(val message: String) : TestPassedDetailScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestPassedDetailScreenEvent()

    object Loading : TestPassedDetailScreenEvent()
}