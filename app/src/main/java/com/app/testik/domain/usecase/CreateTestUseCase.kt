package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.loadedFromServer
import javax.inject.Inject

class CreateTestUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val testRepository: TestRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: TestModel): Result<String> {
        val newData = data.copy(author = authRepository.getCurrentUser()!!.email!!)

        return wrap(
            block = { testRepository.createTest(newData.toDto()) },
            mapper = { it!! }
        ).onSuccess { testId ->
            if (data.image.loadedFromServer() || data.image.isEmpty()) return@onSuccess
            return wrap(
                block = { storageRepository.uploadTestImage(testId, data.image) },
                mapper = { it.toString() }
            ).onSuccess {
                return wrap(
                    block = { testRepository.updateTestImage(testId, it) },
                    mapper = { testId }
                )
            }
        }
    }
}
