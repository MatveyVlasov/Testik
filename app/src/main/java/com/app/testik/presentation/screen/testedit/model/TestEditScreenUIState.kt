package com.app.testik.presentation.screen.testedit.model

import androidx.annotation.StringRes
import com.app.testik.domain.model.TestModel

data class TestEditScreenUIState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val image: String = "",
    val testUpdated: TestModel? = null,
    val canSave: Boolean = true,
    @StringRes val titleError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val categoryError: Int? = null
)