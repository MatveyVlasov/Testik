package com.app.testik.presentation.screen.questlionlist.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class QuestionListScreenUIState(
    val testId: String = "",
    val questions: List<DelegateAdapterItem> = emptyList()
)