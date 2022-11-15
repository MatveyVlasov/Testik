package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.LoginModel
import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(data: LoginModel): Result<Unit> =
        wrap(
            block = { authRepository.loginWithEmail(data.toDto()) },
            mapper = { }
        )
}