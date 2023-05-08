package com.app.testik.data.model

import com.google.firebase.firestore.PropertyName

data class TestDto(
    val id: String = "",
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val image: String = "",
    @get:PropertyName("isPasswordEnabled")
    val isPasswordEnabled: Boolean = false,
    @get:PropertyName("isOpen")
    val isOpen: Boolean = false,
    @get:PropertyName("isPublished")
    val isPublished: Boolean = false,
    @get:PropertyName("isLinkEnabled")
    val isLinkEnabled: Boolean = false,
    val link: String = "",
    @get:PropertyName("isResultsShown")
    val isResultsShown: Boolean = true,
    @get:PropertyName("isCorrectAnswersShown")
    val isCorrectAnswersShown: Boolean = true,
    @get:PropertyName("isCorrectAnswersAfterQuestionShown")
    val isCorrectAnswersAfterQuestionShown: Boolean = true,
    @get:PropertyName("isRetakingEnabled")
    val isRetakingEnabled: Boolean = true,
    @get:PropertyName("isNavigationEnabled")
    val isNavigationEnabled: Boolean = true,
    @get:PropertyName("isRandomQuestions")
    val isRandomQuestions: Boolean = false,
    @get:PropertyName("isRandomAnswers")
    val isRandomAnswers: Boolean = false,
    val timeLimit: Long = 0L,
    val timeLimitQuestion: Long = 0L,
    val questionsNum: Int = 0,
    val pointsMax: Int = 0,
    @get:PropertyName("isGradesEnabled")
    val isGradesEnabled: Boolean = false,
    val grades: List<GradeDto> = emptyList()
)