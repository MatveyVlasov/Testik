package com.app.testik.presentation.screen.testedit.model

import androidx.annotation.StringRes
import com.app.testik.domain.model.CategoryType
import com.app.testik.domain.model.TestModel

data class TestEditScreenUIState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: CategoryType = CategoryType.NOT_SELECTED,
    val image: String = "",
    val isPublished: Boolean = false,
    val isTestLinkEnabled: Boolean = false,
    val testLink: String = "",
    val questionsNum: Int = 0,
    val testUpdated: TestModel? = null,
    val canSave: Boolean = true,
    @StringRes val titleError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val categoryError: Int? = null
)