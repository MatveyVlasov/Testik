package com.app.testik.domain.model

data class QuestionModel(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val explanation: String = "",
    val image: String = "",
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val isRequired: Boolean = false,
    val answers: List<AnswerModel> = emptyList(),
    val enteredAnswer: String = "",
    val pointsMax: Int = 1,
    val pointsEarned: Int = 0
)