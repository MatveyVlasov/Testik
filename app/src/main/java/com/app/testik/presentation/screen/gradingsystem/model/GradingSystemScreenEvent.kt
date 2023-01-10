package com.app.testik.presentation.screen.gradingsystem.model

import androidx.annotation.StringRes

sealed class GradingSystemScreenEvent {

    data class ShowSnackbar(val message: String) : GradingSystemScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : GradingSystemScreenEvent()

    object Loading : GradingSystemScreenEvent()

    data class ErrorOverlappingIntervals(val num: Int) : GradingSystemScreenEvent()
}