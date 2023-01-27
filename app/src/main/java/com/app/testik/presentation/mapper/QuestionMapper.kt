package com.app.testik.presentation.mapper

import com.app.testik.domain.model.AnswerModel
import com.app.testik.domain.model.QuestionModel
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.model.answer.*

fun QuestionModel.toQuestionItem() =
    QuestionDelegateItem(
        id = id,
        testId = testId,
        title = title,
        description = description,
        explanation = explanation,
        image = image,
        type = type,
        isRequired = isRequired,
        answers = answers.map { it.toAnswerItem() },
        enteredAnswer = enteredAnswer,
        isMatch = isMatch,
        isCaseSensitive = isCaseSensitive,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned
    )

fun QuestionDelegateItem.toDomain() =
    QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        explanation = explanation,
        image = image,
        type = type,
        isRequired = isRequired,
        answers = answers.map { it.toDomain() },
        enteredAnswer = enteredAnswer,
        isMatch = isMatch,
        isCaseSensitive = isCaseSensitive,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned
    )

fun AnswerModel.toAnswerItem(): AnswerDelegateItem {
    return when (type) {
        QuestionType.SINGLE_CHOICE ->
            SingleChoiceDelegateItem(
                text = text,
                isCorrect = isCorrect,
                isSelected = isSelected
            )
        QuestionType.MULTIPLE_CHOICE ->
            MultipleChoiceDelegateItem(
                text = text,
                isCorrect = isCorrect,
                isSelected = isSelected
            )
        QuestionType.SHORT_ANSWER -> ShortAnswerDelegateItem(text = text)
        QuestionType.MATCHING -> MatchingDelegateItem(text = text, textMatching = textMatching)
        QuestionType.ORDERING -> OrderingDelegateItem(text = text)
    }
}

fun AnswerDelegateItem.toDomain(): AnswerModel {
    return when (this) {
        is SingleChoiceDelegateItem ->
            AnswerModel(
                text = text,
                isCorrect = isCorrect,
                isSelected = isSelected
            )
        is MultipleChoiceDelegateItem ->
            AnswerModel(
                text = text,
                isCorrect = isCorrect,
                isSelected = isSelected
            )
        is ShortAnswerDelegateItem -> AnswerModel(text = text)
        is MatchingDelegateItem -> AnswerModel(text = text, textMatching = textMatching)
        is OrderingDelegateItem -> AnswerModel(text = text)
        else -> AnswerModel(text = text)
    }
}