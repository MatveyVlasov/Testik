package com.app.testik.presentation.screen.testedit.mapper

import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState
import com.app.testik.util.getMillisFromHoursAndMinutes


fun TestEditScreenUIState.toDomain() =
    TestModel(
        id = id,
        title = title,
        description = description,
        category = category,
        image = image,
        isPasswordEnabled = password.isNotEmpty(),
        isOpen = isOpen,
        isPublished = isPublished,
        isLinkEnabled = isTestLinkEnabled,
        link = testLink,
        isResultsShown = isResultsShown,
        isCorrectAnswersShown = isCorrectAnswersShown,
        isCorrectAnswersAfterQuestionShown = isCorrectAnswersAfterQuestionShown,
        isRetakingEnabled = isRetakingEnabled,
        isNavigationEnabled = isNavigationEnabled,
        isRandomQuestions = isRandomQuestions,
        isRandomAnswers = isRandomAnswers,
        timeLimit = if (isTimeLimitEnabled) getMillisFromHoursAndMinutes(timeLimitHours, timeLimitMinutes) else 0,
        timeLimitQuestion = if (isTimeLimitEnabled) getMillisFromHoursAndMinutes(timeLimitQuestionHours, timeLimitQuestionMinutes) else 0
    )