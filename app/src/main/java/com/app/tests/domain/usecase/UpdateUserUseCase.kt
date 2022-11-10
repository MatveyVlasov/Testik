package com.app.tests.domain.usecase

import com.app.tests.domain.mapper.toDto
import com.app.tests.domain.model.*
import com.app.tests.domain.repository.FirestoreRepository
import com.app.tests.domain.repository.StorageRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
import com.app.tests.util.loadedFromServer
import timber.log.Timber
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: UserModel): Result<Unit> {
        if (data.avatar.isBlank() || data.avatar.loadedFromServer()) return updateUser(data)

        wrap(
            block = { storageRepository.uploadAvatar(data.toDto()) },
            mapper = { it }
        ).onSuccess { avatar ->
            val newData = data.copy(avatar = avatar.toString())
            return updateUser(newData)
        }.onError {
            updateUser(data)
            return Result.Error("Error while saving image")
        }
        return Result.Error("Error occurred")
    }

    private suspend fun updateUser(data: UserModel) =
        wrap(
            block = { firestoreRepository.updateUser(data.toDto()) },
            mapper = { }
        )
}
