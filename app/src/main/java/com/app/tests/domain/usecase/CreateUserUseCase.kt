package com.app.tests.domain.usecase

import com.app.tests.domain.mapper.toDto
import com.app.tests.domain.model.RegistrationModel
import com.app.tests.domain.model.Result
import com.app.tests.domain.model.onError
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.repository.FirestoreRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: RegistrationModel): Result<Unit> {
        return wrap(
            block = { authRepository.signUpWithEmail(data.toDto()) },
            mapper = { }
        ).onSuccess {
            return wrap(
                block = { firestoreRepository.addUser(data.toDto()) },
                mapper = { }
            ).onError {
                authRepository.deleteCurrentUser()
            }
        }
    }
}