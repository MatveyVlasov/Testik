package com.app.testik.presentation.dialog.passwordchange.model

import androidx.annotation.StringRes

data class PasswordChangeDialogUIState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val passwordRepeated: String = "",
    @StringRes val oldPasswordError: Int? = null,
    @StringRes val newPasswordError: Int? = null,
    @StringRes val passwordRepeatedError: Int? = null
) {
    val canChange: Boolean
        get() = oldPassword.isNotEmpty() && newPassword.isNotEmpty() && passwordRepeated.isNotEmpty()
                && oldPasswordError == null && newPasswordError == null && passwordRepeatedError == null

}