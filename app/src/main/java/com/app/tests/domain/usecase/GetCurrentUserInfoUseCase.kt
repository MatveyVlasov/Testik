package com.app.tests.domain.usecase

import com.app.tests.domain.mapper.toDomain
import com.app.tests.domain.model.Result
import com.app.tests.domain.model.UserModel
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.repository.FirestoreRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
import javax.inject.Inject

class GetCurrentUserInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(): Result<UserModel> =
        wrap(
            block = { firestoreRepository.getUserInfo(authRepository.getCurrentUser()?.email) },
            mapper = { it!!.toDomain() }
        )
}