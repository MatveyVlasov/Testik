package com.app.testik.domain.model

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()

    data class Error<T>(val error: String) : Result<T>()
}

inline fun <reified T> Result<T>.onError(block: (error: String) -> Unit): Result<T> {
    if (this is Result.Error) {
        block(error)
    }
    return this
}

inline fun <reified T> Result<T>.onSuccess(block: (data: T) -> Unit): Result<T> {
    if (this is Result.Success) {
        block(data)
    }
    return this
}