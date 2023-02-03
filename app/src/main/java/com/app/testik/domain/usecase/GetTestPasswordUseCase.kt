package com.app.testik.domain.usecase

import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class GetTestPasswordUseCase @Inject constructor(
    private val testRepository: TestRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String): Result<String> =
        wrap(
            block = { testRepository.getPassword(testId = testId) },
            mapper = { it.orEmpty() }
        )
}