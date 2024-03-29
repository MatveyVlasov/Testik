package com.app.testik.presentation.screen.testpasseddetail.model

import com.app.testik.domain.model.ResultsModel
import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestPassedDetailScreenUIState(
    val recordId: String = "",
    val testId: String = "",
    val username: String = "",
    val title: String = "",
    val image: String = "",
    val date: String = "",
    val timeSpent: String = "",
    val isFinished: Boolean = false,
    val isResultsShown: Boolean = true,
    val pointsMax: Int = 0,
    val pointsEarned: Int = 0,
    val pointsCalculated: Boolean = false,
    val pointsHasError: Boolean = false,
    val gradeEarned: String = "",
    val questions: List<DelegateAdapterItem> = emptyList(),
    val results: ResultsModel? = null
)