package com.app.testik.domain.usecase

import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
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