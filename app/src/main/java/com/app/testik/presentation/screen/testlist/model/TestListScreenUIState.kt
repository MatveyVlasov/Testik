package com.app.testik.presentation.screen.testlist.model

import com.app.testik.domain.model.CategoryType
import com.app.testik.domain.model.UserModel
import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestListScreenUIState(
    val category: CategoryType,
    val tests: List<DelegateAdapterItem> = emptyList(),
    val users: List<UserModel> = emptyList(),
    val lastQuery: String = ""
)