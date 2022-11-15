package com.app.testik.presentation.screen.registration.mapper

import com.app.testik.domain.model.RegistrationModel
import com.app.testik.presentation.screen.registration.model.RegistrationScreenUIState

fun RegistrationScreenUIState.toDomain() =
    RegistrationModel(
        email = email,
        password = password,
        username = username
    )