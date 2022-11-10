package com.app.tests.presentation.screen.profile.model

sealed class ProfileScreenEvent {

    data class ShowSnackbar(val message: String) : ProfileScreenEvent()

    object Loading : ProfileScreenEvent()

    object SuccessSignOut : ProfileScreenEvent()

    object SuccessAccountDeletion : ProfileScreenEvent()
}