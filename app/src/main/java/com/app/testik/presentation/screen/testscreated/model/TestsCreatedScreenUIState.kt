package com.app.testik.presentation.screen.testscreated.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestsCreatedScreenUIState(
    val tests: List<DelegateAdapterItem> = emptyList()
)