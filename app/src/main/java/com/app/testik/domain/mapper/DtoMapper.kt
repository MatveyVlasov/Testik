package com.app.testik.domain.mapper

import com.app.testik.data.model.*
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
    val tests = mutableListOf<TestModel>()
    if (!snapshot.isEmpty) {
        for (document in snapshot) {
            val data = document.toObject(TestModel::class.java)
            tests.add(data.copy(id = document.id))
        }
    }

    return TestsModel(
        snapshot = snapshot,
        tests = tests
    )
}

fun TestDto.toDomain() =
    TestModel(
        id = id,
        author = author,
        title = title,
        description = description,
        category = category,
        image = image
    )

fun QuestionDto.toDomain() =
    QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type.toQuestionType(),
        answers = answers.map { it.toDomain(type.toQuestionType()) }
    )

fun AnswerDto.toDomain(type: QuestionType) =
    AnswerModel(
        type = type,
        text = text,
        isCorrect = isCorrect,
    )

fun String.toQuestionType() =
    QuestionType.values().find { it.title == this } ?: QuestionType.SINGLE_CHOICE