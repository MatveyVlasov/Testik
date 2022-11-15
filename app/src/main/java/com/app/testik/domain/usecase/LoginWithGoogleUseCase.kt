package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.RegistrationModel
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.FirestoreRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
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
