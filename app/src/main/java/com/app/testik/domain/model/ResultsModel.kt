package com.app.testik.domain.model

data class ResultsModel(
    val answersCorrect: List<List<AnswerModel>> = emptyList(),
    val pointsPerQuestion: List<Int> = emptyList()
)