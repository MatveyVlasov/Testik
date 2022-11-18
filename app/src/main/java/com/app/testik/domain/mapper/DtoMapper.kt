package com.app.testik.domain.mapper

import com.app.testik.data.model.TestDto
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