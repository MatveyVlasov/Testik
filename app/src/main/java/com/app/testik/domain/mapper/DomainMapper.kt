package com.app.testik.domain.mapper

import com.app.testik.data.model.LoginDto
import com.app.testik.data.model.RegistrationDto
import com.app.testik.data.model.TestDto
import com.app.testik.data.model.UserDto
import com.app.testik.domain.model.LoginModel
import com.app.testik.domain.model.RegistrationModel
import com.app.testik.domain.model.TestModel
import com.app.testik.domain.model.UserModel

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
        category = category,
        image = image
    )