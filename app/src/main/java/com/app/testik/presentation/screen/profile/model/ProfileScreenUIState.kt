package com.app.testik.presentation.screen.profile.model

import androidx.annotation.StringRes

data class ProfileScreenUIState(
    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val avatar: String = "",
    val avatarUpdated: Boolean = false,
    val canSave: Boolean = true,
    @StringRes val usernameError: Int? = null,
    @StringRes val firstNameError: Int? = null,
    @StringRes val lastNameError: Int? = null
)