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
        isPublished = isPublished,
        isLinkEnabled = isTestLinkEnabled,
        link = testLink,
        isRandomQuestions = isRandomQuestions,
        isRandomAnswers = isRandomAnswers
    )