package com.app.tests.domain.util

import com.app.tests.data.model.ApiResult
import com.app.tests.domain.model.Result

class ResultWrapperImpl : ResultWrapper {

    override suspend fun <T, R> wrap(
        block: suspend () -> ApiResult<T>,
        mapper: suspend (data: T?) -> R
    ): Result<R> = when (val result = block()) {
        is ApiResult.Success -> Result.Success(mapper(result.data))
        is ApiResult.Error -> Result.Error(result.error.orEmpty())
    }
}