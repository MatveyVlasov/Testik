package com.app.testik.presentation.dialog.testinfo.model

import com.app.testik.domain.model.CategoryType

data class TestInfoDialogUIState(
    val id: String = "",
    val image: String = "",
    val title: String = "",
    val author: String = "",
    val authorName: String = "",
    val description: String = "",
    val category: CategoryType = CategoryType.NOT_SELECTED,
    val questionsNum: Int = 0
)