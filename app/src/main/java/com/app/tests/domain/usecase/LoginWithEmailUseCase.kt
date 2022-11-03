package com.app.tests.domain.usecase

import com.app.tests.domain.mapper.toDto
import com.app.tests.domain.model.LoginModel
import com.app.tests.domain.model.Result
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
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