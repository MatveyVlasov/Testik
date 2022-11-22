package com.app.testik.presentation.screen.questionedit.mapper

import com.app.testik.domain.model.QuestionModel
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.screen.questlionlist.mapper.toDomain
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenUIState

fun QuestionEditScreenUIState.toDomain() =
    QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type,
        answers = answers.map { it.toDomain() }
    )

fun QuestionEditScreenUIState.toQuestionItem() =
    QuestionDelegateItem(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type,
        answers = answers
    )
