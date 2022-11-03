package com.app.tests.presentation.screen.registration.model

data class RegistrationScreenUIState(
    val email: String = "",
    val password: String = "",
    val passwordRepeated: String = "",
    val username: String = ""
) {
    val canRegister: Boolean
        get() = email.isNotEmpty() && password.isNotEmpty()
                && passwordRepeated.isNotEmpty() && username.isNotEmpty()
}