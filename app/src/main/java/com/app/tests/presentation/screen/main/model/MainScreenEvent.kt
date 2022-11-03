package com.app.tests.presentation.screen.main.model


sealed class MainScreenEvent {

    data class ShowSnackbar(val message: String) : MainScreenEvent()

    object Loading : MainScreenEvent()
}