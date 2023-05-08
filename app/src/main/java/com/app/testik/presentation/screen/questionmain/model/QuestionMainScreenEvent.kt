package com.app.testik.presentation.screen.questionmain.model

import androidx.annotation.StringRes

sealed class QuestionMainScreenEvent {

    data class ShowSnackbar(val message: String) : QuestionMainScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : QuestionMainScreenEvent()

    object Loading : QuestionMainScreenEvent()

    object NavigateToResults : QuestionMainScreenEvent()

    data class NavigateToQuestion(val num: Int) : QuestionMainScreenEvent()

    object NavigateToTestsPassed : QuestionMainScreenEvent()

    data class UnansweredQuestion(val num: Int) : QuestionMainScreenEvent()

    object TimerFinished : QuestionMainScreenEvent()

    object TooLate : QuestionMainScreenEvent()
}