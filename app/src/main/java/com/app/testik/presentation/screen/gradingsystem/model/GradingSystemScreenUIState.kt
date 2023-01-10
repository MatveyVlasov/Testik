package com.app.testik.presentation.screen.gradingsystem.model

data class GradingSystemScreenUIState(
    val testId: String = "",
    val isEnabled: Boolean = false,
    val grades: List<GradeDelegateItem> = emptyList()
)