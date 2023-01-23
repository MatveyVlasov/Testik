package com.app.testik.presentation.screen.questionedit.mapper

import com.app.testik.domain.model.QuestionModel
import com.app.testik.presentation.mapper.toDomain
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenUIState
import com.app.testik.util.toIntOrZero

fun QuestionEditScreenUIState.toDomain() =
    QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type,
        answers = answers.map { it.toDomain() },
        pointsMax = points.toIntOrZero()
    )

fun QuestionEditScreenUIState.toQuestionItem() =
    QuestionDelegateItem(
        id = id,
        testId = testId,
        title = title,
        description = description,
        explanation = explanation,
        image = image,
        type = type,
        isRequired = isRequired,
        answers = answers,
        pointsMax = points.toIntOrZero(),
    )
