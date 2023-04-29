package com.app.testik.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestModel(
    val id: String = "",
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val category: CategoryType = CategoryType.NOT_SELECTED,
    val image: String = "",
    val isPasswordEnabled: Boolean = false,
    val isOpen: Boolean = false,
    val isPublished: Boolean = false,
    val isLinkEnabled: Boolean = false,
    val link: String = "",
    val isResultsShown: Boolean = true,
    val isCorrectAnswersShown: Boolean = true,
    val isCorrectAnswersAfterQuestionShown: Boolean = true,
    val isRetakingEnabled: Boolean = true,
    val isNavigationEnabled: Boolean = true,
    val isRandomQuestions: Boolean = false,
    val isRandomAnswers: Boolean = false,
    val questionsNum: Int = 0,
    val pointsMax: Int = 0
) : Parcelable