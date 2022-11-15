package com.app.testik.presentation.screen.passwordreset.model

import androidx.annotation.StringRes

data class PasswordResetScreenUIState(
    val email: String = "",
    @StringRes val emailError: Int? = null
) {
    val canReset: Boolean
        get() = email.isNotEmpty() && emailError == null
}