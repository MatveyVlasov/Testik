package com.app.testik.presentation.screen.question.model

import androidx.annotation.StringRes

sealed class QuestionScreenEvent {

    data class ShowSnackbar(val message: String) : QuestionScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : QuestionScreenEvent()

    object Loading : QuestionScreenEvent()
}