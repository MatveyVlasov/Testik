package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class UpdateTestPassedUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: TestPassedModel): Result<Unit> {
        return wrap(
            block = { testPassedRepository.updateTest(data.toDto()) },
            mapper = { }
        )
    }
}
