package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class StartTestUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: TestPassedModel): Result<TestPassedModel> {
        val newData = data.copy(user = authRepository.getCurrentUser()!!.uid)

        return wrap(
            block = { testPassedRepository.startTest(newData.toDto()) },
            mapper = { it!!.toDomain() }
        )
    }
}
