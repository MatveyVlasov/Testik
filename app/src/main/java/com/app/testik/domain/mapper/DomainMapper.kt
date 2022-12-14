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
        answersCorrect = map { it.answers.toDto() }
    )

fun QuestionModel.toDto() =
    QuestionDto(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type.title,
        answers = answers.map { it.toDto() },
        enteredAnswer = enteredAnswer,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned
    )

fun List<AnswerModel>.toDto() =
    AnswersCorrectDto(map { it.toDtoCorrect() })

fun AnswerModel.toDto() =
    AnswerDto(
        text = text,
        isSelected = isSelected
    )

fun AnswerModel.toDtoCorrect() =
    AnswerCorrectDto(
        text = text,
        isCorrect = isCorrect
    )

fun GradeModel.toDto() =
    GradeDto(
        grade = grade,
        pointsFrom = pointsFrom,
        pointsTo = pointsTo
    )