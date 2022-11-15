package com.app.testik.presentation.screen.main.model


sealed class MainScreenEvent {

    data class ShowSnackbar(val message: String) : MainScreenEvent()

    object Loading : MainScreenEvent()
}