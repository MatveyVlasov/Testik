package com.app.tests.presentation.screen.profile.model

import androidx.annotation.StringRes

sealed class ProfileScreenEvent {

    data class ShowSnackbar(val message: String) : ProfileScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : ProfileScreenEvent()

    object Loading : ProfileScreenEvent()

    object SuccessSignOut : ProfileScreenEvent()

    object SuccessAccountDeletion : ProfileScreenEvent()

    object Restart : ProfileScreenEvent()
}