package com.app.tests.domain.usecase

import com.app.tests.domain.model.Result
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.repository.FirestoreRepository
import com.app.tests.domain.repository.StorageRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
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