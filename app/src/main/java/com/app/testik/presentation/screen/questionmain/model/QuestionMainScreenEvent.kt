package com.app.testik.presentation.screen.questionmain.model

import androidx.annotation.StringRes

sealed class QuestionMainScreenEvent {

    data class ShowSnackbar(val message: String) : QuestionMainScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : QuestionMainScreenEvent()

    object Loading : QuestionMainScreenEvent()
}