package com.app.testik.domain.util

import com.app.testik.data.model.ApiResult
import com.app.testik.domain.model.Result

interface ResultWrapper {

    suspend fun <T, R> wrap(
        block: suspend () -> ApiResult<T>,
        mapper: suspend (data: T?) -> R
    ): Result<R>
}