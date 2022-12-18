package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.TestsModel
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.Constants.QUERY_LIMIT
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class GetCurrentUserTestsUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val testRepository: TestRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(snapshot: QuerySnapshot?): Result<TestsModel> =
        wrap(
            block = {
                testRepository.getTestsByAuthor(
                    uid = authRepository.getCurrentUser()?.uid,
                    limit = QUERY_LIMIT,
                    snapshot = snapshot
                )
            },
            mapper = { it!!.toDomain() }
        )
}