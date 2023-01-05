package com.app.testik.presentation.screen.testspassedusers.model

import androidx.annotation.StringRes

sealed class TestsPassedUsersScreenEvent {

    data class ShowSnackbar(val message: String) : TestsPassedUsersScreenEvent()

    data class ShowSnackbarByRes(@StringRes val message: Int) : TestsPassedUsersScreenEvent()

    object Loading : TestsPassedUsersScreenEvent()
}