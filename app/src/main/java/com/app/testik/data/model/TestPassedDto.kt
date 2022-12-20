package com.app.testik.data.model

data class TestPassedDto(
    val recordId: String = "",
    val testId: String = "",
    val title: String = "",
    val image: String = "",
    val user: String = "",
    val timeStarted: Long = 0L,
    val timeFinished: Long = 0L,
    val questions: List<EmptyDto> = emptyList(),
    val pointsMax: Int = 0,
    val pointsEarned: Int = 0
)