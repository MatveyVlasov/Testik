package com.app.tests.domain.usecase

import com.app.tests.domain.mapper.toDto
import com.app.tests.domain.model.RegistrationModel
import com.app.tests.domain.model.Result
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.repository.FirestoreRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(credential: AuthCredential, data: RegistrationModel): Result<Unit> {
        wrap(
            block = { authRepository.loginWithGoogle(credential) },
            mapper = { it }
        ).onSuccess { isNewUser ->
            if (isNewUser == true) {
                return wrap(
                    block = { firestoreRepository.addUser(data.toDto()) },
                    mapper = { }
                )
            }
            return Result.Success(Unit)
        }
        return Result.Error("Error occurred")
    }
}
