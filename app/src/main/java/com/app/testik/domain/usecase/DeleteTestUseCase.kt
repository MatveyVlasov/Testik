package com.app.testik.domain.usecase

import com.app.testik.domain.model.Result
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class DeleteTestUseCase @Inject constructor(
    private val testRepository: TestRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String): Result<Unit> =
        wrap(
            block = { testRepository.deleteTest(testId) },
            mapper = { }
        ).onSuccess {
            storageRepository.deleteTest(testId)
        }
}