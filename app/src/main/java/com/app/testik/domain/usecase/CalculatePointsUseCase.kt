package com.app.testik.domain.usecase

import com.app.testik.domain.model.*
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.orZero
import javax.inject.Inject

class CalculatePointsUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(recordId: String): Result<Int> {
        return wrap(
            block = { testPassedRepository.calculatePoints(recordId = recordId) },
            mapper = { it.orZero() }
        )
    }
}
