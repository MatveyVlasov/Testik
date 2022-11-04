package com.app.tests.domain.mapper

import com.app.tests.data.model.LoginDto
import com.app.tests.data.model.RegistrationDto
import com.app.tests.domain.model.LoginModel
import com.app.tests.domain.model.RegistrationModel
import com.app.tests.util.removeWhiteSpaces

fun RegistrationModel.toDto() =
    RegistrationDto(
        email = email,
        password = password,
        username = username.removeWhiteSpaces().lowercase()
    )

fun LoginModel.toDto() =
    LoginDto(
        email = email,
        password = password
    )