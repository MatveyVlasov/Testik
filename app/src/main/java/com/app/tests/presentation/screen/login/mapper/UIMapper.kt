package com.app.tests.presentation.screen.login.mapper

import com.app.tests.domain.model.LoginModel
import com.app.tests.presentation.screen.login.model.LoginScreenUIState

fun LoginScreenUIState.toDomain() =
    LoginModel(
        email = email,
        password = password
    )