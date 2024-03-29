package com.app.testik.data.model

data class TestQuestionsDto(
    val questions: List<QuestionDto> = emptyList(),
    val answersCorrect: List<AnswersCorrectDto> = emptyList(),
    val explanations: List<String> = emptyList()
)