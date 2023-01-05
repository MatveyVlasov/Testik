package com.app.testik.presentation.screen.testspassedusers.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestsPassedUsersScreenUIState(
    val testId: String = "",
    val tests: List<DelegateAdapterItem> = emptyList()
)