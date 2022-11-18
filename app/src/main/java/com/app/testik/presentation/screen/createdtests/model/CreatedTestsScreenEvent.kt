package com.app.testik.presentation.screen.createdtests.model

import androidx.annotation.StringRes

sealed class CreatedTestsScreenEvent {

    data class ShowSnackbar(val message: String) : CreatedTestsScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : CreatedTestsScreenEvent()

    object Loading : CreatedTestsScreenEvent()

    object SuccessTestDeletion : CreatedTestsScreenEvent()
}