package com.app.tests.presentation.screen.registration.model

import androidx.annotation.StringRes

data class RegistrationScreenUIState(
    val email: String = "",
    val password: String = "",
    val passwordRepeated: String = "",
    val username: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val passwordRepeatedError: Int? = null,
    @StringRes val usernameError: Int? = null
) {
    val canRegister: Boolean
        get() = email.isNotEmpty() && password.isNotEmpty() && passwordRepeated.isNotEmpty() && username.isNotEmpty()
                && emailError == null && passwordError == null && passwordRepeatedError == null && usernameError == null
}