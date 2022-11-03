package com.app.tests.data.model

sealed class ApiResult<T> {
    data class Success<T>(val data: T? = null) : ApiResult<T>()

    data class Error<T>(val error: String?) : ApiResult<T>()
}