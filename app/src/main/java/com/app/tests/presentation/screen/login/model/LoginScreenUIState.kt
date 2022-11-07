package com.app.tests.presentation.screen.login.model

import androidx.annotation.StringRes

data class LoginScreenUIState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null
) {
    val canLogin: Boolean
        get() = email.isNotEmpty() && password.isNotEmpty() && emailError == null && passwordError == null
}