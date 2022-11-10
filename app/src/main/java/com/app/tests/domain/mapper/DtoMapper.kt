package com.app.tests.domain.mapper

import com.app.tests.data.model.UserDto
import com.app.tests.domain.model.UserModel

fun UserDto.toDomain() =
    UserModel(
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        avatar = avatar
    )