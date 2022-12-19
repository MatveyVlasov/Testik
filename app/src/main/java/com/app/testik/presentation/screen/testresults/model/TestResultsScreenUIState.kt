package com.app.testik.presentation.screen.testresults.model

data class TestResultsScreenUIState(
    val recordId: String = "",
    val testId: String = "",
    val title: String = "",
    val image: String = "",
    val date: String = "",
    val timeSpent: String = "",
    val points: Int = 0,
    val pointsMax: Int = 0
)