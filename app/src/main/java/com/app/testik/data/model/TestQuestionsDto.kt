package com.app.testik.data.model

data class TestQuestionsDto(
    val id: String = "",
    val questions: List<QuestionDto> = emptyList()
)