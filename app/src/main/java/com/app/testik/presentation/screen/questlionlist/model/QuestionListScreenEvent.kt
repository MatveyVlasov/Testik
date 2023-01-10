package com.app.testik.presentation.screen.questlionlist.model

import androidx.annotation.StringRes

sealed class QuestionListScreenEvent {

    data class ShowSnackbar(val message: String) : QuestionListScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : QuestionListScreenEvent()

    object Loading : QuestionListScreenEvent()

    data class SuccessQuestionsSave(val questionsNum: Int) : QuestionListScreenEvent()
}