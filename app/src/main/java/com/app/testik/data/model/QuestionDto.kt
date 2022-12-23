package com.app.testik.data.model

data class QuestionDto(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val type: String = "",
    val answers: List<AnswerDto> = emptyList(),
    val enteredAnswer: String = "",
    val pointsMax: Int = 1,
    val pointsEarned: Int = 0
)