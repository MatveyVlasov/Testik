package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.loadedFromServer
import javax.inject.Inject

class UpdateTestUseCase @Inject constructor(
    private val testRepository: TestRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: TestModel): Result<Unit> {
        if (data.image.isEmpty() || data.image.loadedFromServer()) return updateTest(data)

        wrap(
            block = { storageRepository.uploadTestImage(testId = data.id, image = data.image) },
            mapper = { it.orEmpty() }
        ).onSuccess { image ->
            val newData = data.copy(image = image)
            return updateTest(newData)
        }.onError {
            return updateTest(data).onSuccess {
                return Result.Error("Error while saving image")
            }
        }
        return Result.Error("Error occurred")
    }

    private suspend fun updateTest(data: TestModel) =
        wrap(
            block = { testRepository.updateTest(data.toDto()) },
            mapper = { }
        )
}
