package com.app.testik.presentation.screen.questionmain.model

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.model.QuestionDelegateItem

data class QuestionMainScreenUIState(
    val test: TestPassedModel,
    val questions: List<QuestionDelegateItem> = emptyList(),
    val isReviewMode: Boolean = false,
    val isReviewQuestionMode: Boolean = false,
    val startQuestion: Int = 0,
    val currentQuestion: Int = 0,
    val isTimerEnabled: Boolean = false,
    val isTimerQuestionEnabled: Boolean = false,
    val timeLimit: Long = 0L,
    val isTimerFinishedHandled: Boolean = true,
    val isNavigationToResultsAllowed: Boolean = true,
    val navigateToResultsOnFinish: Boolean = false
)