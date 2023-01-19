package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.randomId
import javax.inject.Inject

class CreateTestUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val testRepository: TestRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: TestModel): Result<TestCreationModel> {
        val testId = randomId

        wrap(
            block = { storageRepository.uploadTestImage(testId = testId, image = data.image) },
            mapper = { it.orEmpty() }
        ).onSuccess {
            return wrap(
                block = {
                    val newData = data.copy(
                        author = authRepository.getCurrentUser()!!.uid,
                        image = it
                    )
                    testRepository.createTest(testId = testId, data = newData.toDto())
                },
                mapper = { it!!.toDomain() }
            )
        }

        return Result.Error("An error occurred")
    }
}
