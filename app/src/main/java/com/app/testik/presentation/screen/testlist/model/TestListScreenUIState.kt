package com.app.testik.presentation.screen.testlist.model

import com.app.testik.domain.model.CategoryType
import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestListScreenUIState(
    val category: CategoryType,
    val tests: List<DelegateAdapterItem> = emptyList()
)