package com.app.testik.data.model

sealed class ApiResult<T> {
    data class Success<T>(val data: T? = null) : ApiResult<T>()

    open class Error<T>(open val error: String?) : ApiResult<T>()

    data class NoInternetError<T>(override val error: String? = "No internet connection") : Error<T>(error)
}