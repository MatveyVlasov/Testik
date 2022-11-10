package com.app.tests.presentation.screen.profile.mapper

import com.app.tests.domain.model.UserModel
import com.app.tests.presentation.screen.profile.model.ProfileScreenUIState
import com.app.tests.util.removeExtraSpaces
import com.app.tests.util.toUsername

fun ProfileScreenUIState.toDomain() =
    UserModel(
        email = email,
        username = username.toUsername(),
        firstName = firstName.removeExtraSpaces(),
        lastName = lastName.removeExtraSpaces(),
        avatar = avatar
    )