package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.TestPassedModel
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.google.firebase.firestore.Source
import javax.inject.Inject

class GetTestPassedInfoUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(recordId: String, source: Source): Result<TestPassedModel> =
        wrap(
            block = { testPassedRepository.getTest(recordId = recordId, source = source) },
            mapper = { it!!.toDomain() }
        )
}