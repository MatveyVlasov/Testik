package com.app.testik.data.model

data class ResultsDto(
    val answersCorrect: List<AnswersCorrectDto> = emptyList(),
    val pointsPerQuestion: List<Int> = emptyList(),
    val explanations: List<String> = emptyList()
)