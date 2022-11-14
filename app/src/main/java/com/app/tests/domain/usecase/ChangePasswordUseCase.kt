package com.app.tests.domain.usecase

import com.app.tests.domain.model.Result
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(oldPassword: String, newPassword: String): Result<Unit> =
        wrap(
            block = { authRepository.changePassword(oldPassword = oldPassword, newPassword = newPassword) },
            mapper = { }
        )
}