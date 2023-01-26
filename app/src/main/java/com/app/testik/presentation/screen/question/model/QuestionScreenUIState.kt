package com.app.testik.presentation.screen.question.model

import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.model.AnswerDelegateItem

data class QuestionScreenUIState(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val explanation: String = "",
    val image: String = "",
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val isRequired: Boolean = false,
    val answers: List<AnswerDelegateItem> = emptyList(),
    val answersMatching: List<AnswerDelegateItem> = emptyList(),
    val enteredAnswer: String = "",
    val isMatch: Boolean = false,
    val isCaseSensitive: Boolean = false,
    val pointsMax: Int = 1,
    val pointsEarned: Int = 0
)