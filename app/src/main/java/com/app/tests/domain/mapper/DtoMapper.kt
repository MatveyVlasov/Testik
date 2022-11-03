package com.app.tests.domain.mapper

import com.app.tests.data.model.RegistrationDto
import com.app.tests.domain.model.RegistrationModel

fun RegistrationModel.toDto() =
    RegistrationDto(
        email = email,
        password = password,
        username = username
    )