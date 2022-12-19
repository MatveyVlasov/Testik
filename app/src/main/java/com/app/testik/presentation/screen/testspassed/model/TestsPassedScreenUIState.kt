package com.app.testik.presentation.screen.testspassed.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestsPassedScreenUIState(
    val tests: List<DelegateAdapterItem> = emptyList()
)