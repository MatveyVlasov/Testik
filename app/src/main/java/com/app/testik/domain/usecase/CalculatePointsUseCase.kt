package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class CalculatePointsUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(recordId: String): Result<PointsEarnedModel> {
        return wrap(
            block = { testPassedRepository.calculatePoints(recordId = recordId) },
            mapper = { it!!.toDomain() }
        )
    }
}
