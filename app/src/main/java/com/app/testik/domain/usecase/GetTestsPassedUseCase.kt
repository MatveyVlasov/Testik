package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.TestsPassedModel
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.Constants.QUERY_LIMIT
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class GetTestsPassedUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String, snapshot: QuerySnapshot?, user: String? = null): Result<TestsPassedModel> =
        wrap(
            block = {
                testPassedRepository.getTests(
                    testId = testId,
                    limit = QUERY_LIMIT,
                    snapshot = snapshot,
                    user = user
                )
            },
            mapper = { it!!.toDomain() }
        )
}