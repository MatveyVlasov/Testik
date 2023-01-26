package com.app.testik.domain.mapper

import com.app.testik.data.model.*
import com.app.testik.data.model.TestDto
import com.app.testik.domain.model.*

fun RegistrationModel.toDto() =
    RegistrationDto(
        email = email,
        password = password,
        username = username,
        avatar = avatar
    )

fun LoginModel.toDto() =
    LoginDto(
        email = email,
        password = password
    )

fun UserModel.toDto() =
    UserDto(
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        avatar = avatar
    )

fun TestModel.toDto() =
    TestDto(
        id = id,
        author = author,
        title = title,
        description = description,
        category = category.title,
        image = image,
        isPublished = isPublished,
        isLinkEnabled = isLinkEnabled,
        link = link,
        questionsNum = questionsNum,
        pointsMax = pointsMax
    )

fun TestPassedModel.toDto() =
    TestPassedDto(
        recordId = recordId,
        testId = testId,
        title = title,
        image = image,
        user = user,
        timeStarted = timeStarted,
        timeFinished = timeFinished,
        isFinished = isFinished,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned,
        pointsCalculated = pointsCalculated,
        gradeEarned = gradeEarned,
        isDemo = isDemo
    )

fun List<QuestionModel>.toDto() =
    TestQuestionsDto(
        questions = map { it.toDto() },
        answersCorrect = map { it.answers.toDto() },
        explanations = map { it.explanation }
    )

fun QuestionModel.toDto(): QuestionDto {
    val answers = when (type) {
        QuestionType.SHORT_ANSWER -> emptyList()
        else -> answers.map { it.toDto() }
    }

    return QuestionDto(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type.title,
        isRequired = isRequired,
        answers = answers,
        enteredAnswer = enteredAnswer,
        isMatch = isMatch,
        isCaseSensitive = isCaseSensitive,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned
    )
}

fun List<AnswerModel>.toDto() =
    AnswersCorrectDto(map { it.toDtoCorrect() })

fun AnswerModel.toDto() =
    AnswerDto(
        text = text,
        textMatching = textMatching,
        isSelected = isSelected
    )

fun AnswerModel.toDtoCorrect() =
    AnswerCorrectDto(
        text = text,
        textMatching = textMatching,
        isCorrect = isCorrect
    )

fun GradeModel.toDto() =
    GradeDto(
        grade = grade,
        pointsFrom = pointsFrom,
        pointsTo = pointsTo
    )