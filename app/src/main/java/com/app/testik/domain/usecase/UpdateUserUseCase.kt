package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.UserRepository
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.loadedFromServer
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: UserModel): Result<Unit> {
        val uid = authRepository.getCurrentUser()!!.uid
        if (data.avatar.isEmpty() || data.avatar.loadedFromServer()) return updateUser(data = data, uid = uid)

        wrap(
            block = { storageRepository.uploadAvatar(uid = uid, image = data.avatar) },
            mapper = { it.toString() }
        ).onSuccess { avatar ->
            val newData = data.copy(avatar = avatar)
            return updateUser(data = newData, uid = uid)
        }.onError {
            return updateUser(data = data, uid = uid).onSuccess {
                return Result.Error("Error while saving image")
            }
        }
        return Result.Error("Error occurred")
    }

    private suspend fun updateUser(data: UserModel, uid: String) =
        wrap(
            block = { userRepository.updateUser(data = data.toDto(), uid = uid) },
            mapper = { }
        )
}
