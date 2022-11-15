package com.app.testik.presentation.screen.profile.mapper

import com.app.testik.domain.model.UserModel
import com.app.testik.presentation.screen.profile.model.ProfileScreenUIState
import com.app.testik.util.removeExtraSpaces
import com.app.testik.util.toUsername

fun ProfileScreenUIState.toDomain() =
    UserModel(
        email = email,
        username = username.toUsername(),
        firstName = firstName.removeExtraSpaces(),
        lastName = lastName.removeExtraSpaces(),
        avatar = avatar
    )