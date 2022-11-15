package com.app.testik.presentation.screen.registration.model

import androidx.annotation.StringRes

sealed class RegistrationScreenEvent {

    data class ShowSnackbar(val message: String) : RegistrationScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : RegistrationScreenEvent()

    object Loading : RegistrationScreenEvent()

    object SuccessRegistration : RegistrationScreenEvent()
}