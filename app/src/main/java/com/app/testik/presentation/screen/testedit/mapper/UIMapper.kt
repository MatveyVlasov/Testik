package com.app.testik.presentation.screen.testedit.mapper

import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState


fun TestEditScreenUIState.toDomain() =
    TestModel(
        id = id,
        title = title,
        description = description,
        category = category,
        image = image,
        isPasswordEnabled = password.isNotEmpty(),
        isPublished = isPublished,
        isLinkEnabled = isTestLinkEnabled,
        link = testLink,
        isResultsShown = isResultsShown,
        isCorrectAnswersShown = isCorrectAnswersShown,
        isCorrectAnswersAfterQuestionShown = isCorrectAnswersAfterQuestionShown,
        isNavigationEnabled = isNavigationEnabled,
        isRandomQuestions = isRandomQuestions,
        isRandomAnswers = isRandomAnswers
    )