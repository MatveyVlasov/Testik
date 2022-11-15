package com.app.testik.presentation.screen.login.mapper

import com.app.testik.domain.model.LoginModel
import com.app.testik.presentation.screen.login.model.LoginScreenUIState

fun LoginScreenUIState.toDomain() =
    LoginModel(
        email = email,
        password = password
    )