package com.app.testik.presentation.screen.questionmain.model

import com.app.testik.presentation.model.QuestionDelegateItem

data class QuestionMainScreenUIState(
    val testId: String = "",
    val questions: List<QuestionDelegateItem> = emptyList()
)