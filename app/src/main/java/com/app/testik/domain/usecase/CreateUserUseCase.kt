package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.RegistrationModel
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.UserRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: RegistrationModel): Result<Unit> {
        return wrap(
            block = { authRepository.signUpWithEmail(data.toDto()) },
            mapper = { }
        ).onSuccess {
            return wrap(
                block = { userRepository.addUser(data.toDto()) },
                mapper = { }
            ).onError {
                authRepository.deleteCurrentUser()
            }
        }
    }
}