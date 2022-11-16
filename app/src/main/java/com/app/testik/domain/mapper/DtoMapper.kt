package com.app.testik.domain.mapper

import com.app.testik.data.model.TestsDto
import com.app.testik.data.model.UserDto
import com.app.testik.domain.model.TestModel
import com.app.testik.domain.model.TestsModel
import com.app.testik.domain.model.UserModel

fun UserDto.toDomain() =
    UserModel(
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        avatar = avatar
    )

fun TestsDto.toDomain(): TestsModel =
    TestsModel(
        snapshot = snapshot,
        tests = snapshot.toObjects(TestModel::class.java)
    )