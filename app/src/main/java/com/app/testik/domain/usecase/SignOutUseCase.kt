package com.app.testik.domain.usecase

import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
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