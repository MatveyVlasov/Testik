package com.app.testik.presentation.screen.testpasseddetail.model

import com.app.testik.domain.model.ResultsModel
import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestPassedDetailScreenUIState(
    val recordId: String = "",
    val testId: String = "",
    val title: String = "",
    val image: String = "",
    val date: String = "",
    val timeSpent: String = "",
    val isFinished: Boolean = false,
    val pointsMax: Int = 0,
    val pointsEarned: Int = 0,
    val pointsCalculated: Boolean = false,
    val questions: List<DelegateAdapterItem> = emptyList(),
    val results: ResultsModel? = null
)