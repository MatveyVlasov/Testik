package com.app.testik.domain.mapper

import com.app.testik.data.model.UserDto
import com.app.testik.domain.model.UserModel

fun UserDto.toDomain() =
    UserModel(
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        avatar = avatar
    )