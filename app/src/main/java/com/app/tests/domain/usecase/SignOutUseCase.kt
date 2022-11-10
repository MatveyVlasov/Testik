package com.app.tests.domain.usecase

import com.app.tests.domain.model.Result
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(): Result<Unit> =
        wrap(
            block = { authRepository.signOut() },
            mapper = { }
        )
}