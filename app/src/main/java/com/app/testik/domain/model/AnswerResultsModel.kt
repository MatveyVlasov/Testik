package com.app.testik.domain.model

data class AnswerResultsModel(
    val points: Int = 0,
    val isLateSubmission: Boolean = false,
    val pointsEarned: Int = 0,
    val answersCorrect: List<AnswerModel> = emptyList(),
    val explanation: String = ""
)