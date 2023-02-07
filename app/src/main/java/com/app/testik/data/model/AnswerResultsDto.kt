package com.app.testik.data.model

data class AnswerResultsDto(
    val points: Int = 0,
    val pointsEarned: Int = 0,
    val answersCorrect: AnswersCorrectDto? = null,
    val explanation: String = ""
)