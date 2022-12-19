package com.app.testik.presentation.screen.questionmain.mapper

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.screen.questionmain.model.QuestionMainScreenUIState

fun QuestionMainScreenUIState.toDomain() =
    TestPassedModel(
        recordId = recordId,
        testId = testId
    )