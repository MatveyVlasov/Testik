package com.app.tests.domain.util

import com.app.tests.data.model.ApiResult
import com.app.tests.domain.model.Result

interface ResultWrapper {

    suspend fun <T, R> wrap(
        block: suspend () -> ApiResult<T>,
        mapper: suspend (data: T?) -> R
    ): Result<R>
}