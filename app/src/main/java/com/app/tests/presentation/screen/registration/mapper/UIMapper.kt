package com.app.tests.presentation.screen.registration.mapper

import com.app.tests.domain.model.RegistrationModel
import com.app.tests.presentation.screen.registration.model.RegistrationScreenUIState

fun RegistrationScreenUIState.toDomain() =
    RegistrationModel(
        email = email,
        password = password,
        username = username
    )