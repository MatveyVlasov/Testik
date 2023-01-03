package com.app.testik.presentation.screen.questionmain.model

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.model.QuestionDelegateItem

data class QuestionMainScreenUIState(
    val test: TestPassedModel,
    val questions: List<QuestionDelegateItem> = emptyList(),
    val isReviewMode: Boolean = false,
    val startQuestion: Int = 0
)