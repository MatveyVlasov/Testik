package com.app.testik.domain.model

data class QuestionModel(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val points: Int = 1,
    val image: String = "",
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val answers: List<AnswerModel> = emptyList(),
    val enteredAnswer: String = ""
)