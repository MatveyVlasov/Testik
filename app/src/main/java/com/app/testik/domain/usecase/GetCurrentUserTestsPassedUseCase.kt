package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.TestsPassedModel
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.Constants.QUERY_LIMIT
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class GetCurrentUserTestsPassedUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(snapshot: QuerySnapshot?): Result<TestsPassedModel> =
        wrap(
            block = {
                testPassedRepository.getTestsByUser(
                    uid = authRepository.getCurrentUser()?.uid,
                    limit = QUERY_LIMIT,
                    snapshot = snapshot
                )
            },
            mapper = { it!!.toDomain() }
        )
}