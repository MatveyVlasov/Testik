package com.app.testik.domain.model


data class AnswerModel(
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val text: String = "",
    val isCorrect: Boolean = false
)