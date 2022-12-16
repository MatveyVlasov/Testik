package com.app.testik.data.model

data class TestPassedDto(
    val id: String = "",
    val testId: String = "",
    val user: String = "",
    val timeStarted: String = "",
    val timeFinished: String = "",
    val questions: List<EmptyDto> = emptyList()
)