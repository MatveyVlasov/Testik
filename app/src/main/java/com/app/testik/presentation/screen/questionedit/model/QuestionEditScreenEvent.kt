package com.app.testik.presentation.screen.questionedit.model

import androidx.annotation.StringRes

sealed class QuestionEditScreenEvent {

    data class ShowSnackbar(val message: String) : QuestionEditScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : QuestionEditScreenEvent()

    object Loading : QuestionEditScreenEvent()
}