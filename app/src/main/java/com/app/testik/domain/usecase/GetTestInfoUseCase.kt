package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.TestModel
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.google.firebase.firestore.Source
import javax.inject.Inject

class GetTestInfoUseCase @Inject constructor(
    private val testRepository: TestRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String, source: Source): Result<TestModel> =
        wrap(
            block = { testRepository.getTest(testId = testId, source = source) },
            mapper = { it!!.toDomain() }
        )
}