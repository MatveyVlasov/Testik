package com.app.testik.domain.usecase

import com.app.testik.domain.model.Result
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.FirestoreRepository
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(email: String): Result<Unit> {
        return wrap(
            block = { authRepository.deleteCurrentUser() },
            mapper = { }
        ).onSuccess {
            firestoreRepository.deleteUser(email)
            storageRepository.deleteAvatar(email)
        }
    }
}