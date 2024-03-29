package com.app.testik.presentation.screen.testresults.model

data class TestResultsScreenUIState(
    val recordId: String = "",
    val testId: String = "",
    val title: String = "",
    val date: String = "",
    val timeSpent: String = "",
    val isResultsShown: Boolean = true,
    val pointsMax: Int = 0,
    val pointsEarned: Int = 0,
    val gradeEarned: String = ""
)