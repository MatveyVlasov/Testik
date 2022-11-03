package com.app.tests.presentation.screen.login.model

data class LoginScreenUIState(
    val email: String = "",
    val password: String = ""
) {
    val canLogin: Boolean
        get() = email.isNotEmpty() && password.isNotEmpty()
}