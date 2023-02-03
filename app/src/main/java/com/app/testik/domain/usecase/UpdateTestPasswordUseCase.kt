package com.app.testik.domain.usecase

import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class UpdateTestPasswordUseCase @Inject constructor(
    private val testRepository: TestRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String, password: String): Result<Unit> =
        wrap(
            block = { testRepository.updatePassword(testId = testId, password = password) },
            mapper = { }
        )
}