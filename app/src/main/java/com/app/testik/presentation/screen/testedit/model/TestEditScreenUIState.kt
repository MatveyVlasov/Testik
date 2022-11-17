package com.app.testik.presentation.screen.testedit.model

import androidx.annotation.StringRes

data class TestEditScreenUIState(
    val id: Int = -1,
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val imageUpdated: Boolean = false,
    val canSave: Boolean = true,
    @StringRes val descriptionError: Int? = null
)