package com.app.tests.presentation.screen.passwordreset.model

data class PasswordResetScreenUIState(
    val email: String = ""
) {
    val canReset: Boolean
        get() = email.isNotEmpty()
}