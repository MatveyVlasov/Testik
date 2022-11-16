package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.UserRepository
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.loadedFromServer
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
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
            return updateUser(data).onSuccess {
                return Result.Error("Error while saving image")
            }
        }
        return Result.Error("Error occurred")
    }

    private suspend fun updateUser(data: UserModel) =
        wrap(
            block = { userRepository.updateUser(data.toDto()) },
            mapper = { }
        )
}
