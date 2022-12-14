package com.app.testik.domain.usecase

import com.app.testik.domain.model.Result
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.UserRepository
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(): Result<Unit> {
        val uid = authRepository.getCurrentUser()?.uid ?: return Result.Error("An error occurred")

        return wrap(
            block = { userRepository.deleteUser(uid) },
            mapper = { }
        ).onSuccess {
            storageRepository.deleteAvatar(uid)
            authRepository.deleteCurrentUser()
        }
    }
}