package com.app.tests.presentation.screen.registration.model

sealed class RegistrationScreenEvent {

    data class ShowSnackbar(val message: String) : RegistrationScreenEvent()

    object Loading : RegistrationScreenEvent()

    object SuccessRegistration : RegistrationScreenEvent()
}