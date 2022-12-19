package com.app.testik.presentation.screen.main.model

import com.app.testik.domain.model.CategoryType

data class MainScreenUIState(
    val avatar: String = "",
    val categoryTests: List<TestsCategoryDelegateItem> =
        CategoryType.values()
            .filter { it.title.isNotEmpty() }
            .map { TestsCategoryDelegateItem(category = it) }
)