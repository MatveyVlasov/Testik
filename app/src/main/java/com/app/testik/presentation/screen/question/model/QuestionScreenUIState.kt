package com.app.testik.presentation.screen.question.model

import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.model.AnswerDelegateItem

data class QuestionScreenUIState(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val points: Int = 1,
    val image: String = "",
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val answers: List<AnswerDelegateItem> = emptyList()
)