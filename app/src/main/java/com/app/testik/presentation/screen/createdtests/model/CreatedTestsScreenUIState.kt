package com.app.testik.presentation.screen.createdtests.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class CreatedTestsScreenUIState(
    val tests: MutableList<DelegateAdapterItem> = mutableListOf()
)