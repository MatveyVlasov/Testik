package com.app.testik.presentation.screen.questionedit.model

import androidx.annotation.StringRes

data class QuestionEditScreenUIState(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val canDiscard: Boolean = true,
    @StringRes val titleError: Int? = null,
    @StringRes val descriptionError: Int? = null
)