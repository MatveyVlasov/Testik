package com.app.tests.presentation.screen.profile.model

import androidx.annotation.StringRes

data class ProfileScreenUIState(
    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val avatar: String = "",
    val avatarUpdated: Boolean = false,
    @StringRes val usernameError: Int? = null,
    @StringRes val firstNameError: Int? = null,
    @StringRes val lastNameError: Int? = null
)