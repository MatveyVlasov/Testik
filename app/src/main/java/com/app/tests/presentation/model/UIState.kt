package com.app.tests.presentation.model

sealed class UIState<out T> {
    data class Success<out T>(val data: T) : UIState<T>()

    data class Error<out T>(val message: String?) : UIState<T>()

    object Loading : UIState<Nothing>()
}

inline fun <reified T> UIState<T>.onError(block: (message: String?) -> Unit): UIState<T> {
    if (this is UIState.Error) {
        block(message)
    }
    return this
}

inline fun <reified T> UIState<T>.onSuccess(block: (value: T) -> Unit): UIState<T> {
    if (this is UIState.Success) {
        block(data)
    }
    return this
}

inline fun <reified T> UIState<T>.onLoading(block: () -> Unit): UIState<T> {
    if (this is UIState.Loading) {
        block()
    }
    return this
}