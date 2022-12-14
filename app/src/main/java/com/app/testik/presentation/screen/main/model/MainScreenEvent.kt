package com.app.testik.presentation.screen.main.model

import androidx.annotation.StringRes


sealed class MainScreenEvent {

    data class ShowSnackbar(val message: String) : MainScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : MainScreenEvent()

    object Loading : MainScreenEvent()
}