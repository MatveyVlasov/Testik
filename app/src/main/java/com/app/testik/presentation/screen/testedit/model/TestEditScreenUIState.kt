package com.app.testik.presentation.screen.testedit.model

import androidx.annotation.StringRes
import com.app.testik.domain.model.CategoryType
import com.app.testik.domain.model.TestModel

data class TestEditScreenUIState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val password: String = "",
    val category: CategoryType = CategoryType.NOT_SELECTED,
    val image: String = "",
    val isOpen: Boolean = false,
    val isPublished: Boolean = false,
    val isTestLinkEnabled: Boolean = false,
    val testLink: String = "",
    val isResultsShown: Boolean = true,
    val isCorrectAnswersShown: Boolean = true,
    val isCorrectAnswersAfterQuestionShown: Boolean = false,
    val isRetakingEnabled: Boolean = true,
    val isNavigationEnabled: Boolean = true,
    val isRandomQuestions: Boolean = false,
    val isRandomAnswers: Boolean = false,
    val isTimeLimitEnabled: Boolean = false,
    val timeLimitHours: Int = 0,
    val timeLimitMinutes: Int = 0,
    val timeLimitQuestionHours: Int = 0,
    val timeLimitQuestionMinutes: Int = 0,
    val questionsNum: Int = 0,
    val testUpdated: TestModel? = null,
    val canSave: Boolean = false,
    val showMore: Boolean = false,
    @StringRes val titleError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val categoryError: Int? = null
)