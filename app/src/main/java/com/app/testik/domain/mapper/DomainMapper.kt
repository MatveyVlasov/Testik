package com.app.testik.domain.mapper

import com.app.testik.data.model.*
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
        isPublished = isPublished
    )

fun QuestionModel.toDto() =
    QuestionDto(
        id = id,
        testId = testId,
        title = title,
        description = description,
        image = image,
        type = type.title,
        answers = answers.map { it.toDto() }
    )

fun AnswerModel.toDto() =
    AnswerDto(
        text = text,
        isCorrect = isCorrect
    )