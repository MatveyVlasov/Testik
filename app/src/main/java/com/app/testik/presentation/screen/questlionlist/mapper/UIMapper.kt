package com.app.testik.presentation.screen.questlionlist.mapper

import com.app.testik.domain.model.AnswerModel
import com.app.testik.domain.model.QuestionModel
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.screen.questionedit.model.MultipleChoiceDelegateItem
import com.app.testik.presentation.screen.questionedit.model.SingleChoiceDelegateItem

fun QuestionModel.toQuestionItem() =
    QuestionDelegateItem(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type,
        answers = answers.map { it.toAnswerItem() }
    )

fun QuestionDelegateItem.toDomain() =
    QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type,
        answers = answers.map { it.toDomain() }
    )

fun AnswerModel.toAnswerItem(): AnswerDelegateItem {
    return when (type) {
        QuestionType.SINGLE_CHOICE ->
            SingleChoiceDelegateItem(
                text = text,
                isCorrect = isCorrect
            )
        QuestionType.MULTIPLE_CHOICE ->
            MultipleChoiceDelegateItem(
                text = text,
                isCorrect = isCorrect
            )
    }
}

fun AnswerDelegateItem.toDomain(): AnswerModel {
    return when (this) {
        is SingleChoiceDelegateItem ->
            AnswerModel(
                text = text,
                isCorrect = isCorrect
            )
        is MultipleChoiceDelegateItem ->
            AnswerModel(
                text = text,
                isCorrect = isCorrect
            )
        else -> AnswerModel(text = text)
    }
}