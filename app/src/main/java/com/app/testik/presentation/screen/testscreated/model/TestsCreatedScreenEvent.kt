package com.app.testik.presentation.screen.testscreated.model

import androidx.annotation.StringRes

sealed class TestsCreatedScreenEvent {

    data class ShowSnackbar(val message: String) : TestsCreatedScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestsCreatedScreenEvent()

    object Loading : TestsCreatedScreenEvent()

    data class SuccessTestDeletion(val test: TestCreatedDelegateItem) : TestsCreatedScreenEvent()
}