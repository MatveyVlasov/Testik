package com.app.testik.domain.mapper

import com.app.testik.data.model.*
import com.app.testik.data.model.TestDto
import com.app.testik.domain.model.*

fun UserDto.toDomain() =
    UserModel(
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        avatar = avatar
    )

fun TestsDto.toDomain(): TestsModel {
    val tests = mutableListOf<TestDto>()
    if (!snapshot.isEmpty) {
        for (document in snapshot) {
            val data = document.toObject(TestDto::class.java)
            tests.add(data.copy(id = document.id))
        }
    }

    return TestsModel(
        snapshot = snapshot,
        tests = tests.map { it.toDomain() }
    )
}

fun TestDto.toDomain() =
    TestModel(
        id = id,
        author = author,
        title = title,
        description = description,
        category = category.toCategoryType(),
        image = image,
        isPublished = isPublished,
        questionsNum = questions.size
    )

fun TestPassedDto.toDomain() =
    TestPassedModel(
        id = id,
        testId = testId,
        user = user,
        timeStarted = timeStarted,
        timeFinished = timeFinished
    )

fun QuestionDto.toDomain() =
    QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type.toQuestionType(),
        answers = answers.map { it.toDomain(type.toQuestionType()) },
        enteredAnswer = enteredAnswer
    )

fun AnswerDto.toDomain(type: QuestionType) =
    AnswerModel(
        type = type,
        text = text,
        isCorrect = isCorrect,
        isSelected = isSelected
    )

fun String.toCategoryType() =
    CategoryType.values().find { it.title == this } ?: CategoryType.NOT_SELECTED

fun String.toQuestionType() =
    QuestionType.values().find { it.title == this } ?: QuestionType.SINGLE_CHOICE
