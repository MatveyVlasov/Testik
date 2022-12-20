package com.app.testik.data.model

data class QuestionDto(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val points: Int = 1,
    val image: String = "",
    val type: String = "",
    val answers: List<AnswerDto> = emptyList(),
    val enteredAnswer: String = ""
)