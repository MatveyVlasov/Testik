package com.app.tests.presentation.screen.profile.mapper

import com.app.tests.domain.model.UserModel
import com.app.tests.presentation.screen.profile.model.ProfileScreenUIState

fun ProfileScreenUIState.toDomain() =
    UserModel(
        email = email,
        username = username,
        avatar = avatar
    )