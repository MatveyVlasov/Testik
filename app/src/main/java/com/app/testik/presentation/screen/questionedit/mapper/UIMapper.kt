package com.app.testik.presentation.screen.questionedit.mapper

import com.app.testik.domain.model.QuestionModel
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenUIState

fun QuestionEditScreenUIState.toDomain() =
    QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image
    )