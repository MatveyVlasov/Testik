package com.app.testik.presentation.screen.questionedit.model

import androidx.annotation.StringRes
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.model.AnswerDelegateItem

data class QuestionEditScreenUIState(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val points: String = "",
    val image: String = "",
    val canDiscard: Boolean = true,
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val answers: List<AnswerDelegateItem> = emptyList(),
    @StringRes val titleError: Int? = null,
    @StringRes val descriptionError: Int? = null
)