package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.UserModel
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.UserRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.google.firebase.firestore.Source
import javax.inject.Inject

class GetCurrentUserInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(source: Source = Source.DEFAULT): Result<UserModel> =
        wrap(
            block = {
                userRepository.getUserInfo(
                    uid = authRepository.getCurrentUser()?.uid,
                    source = source
                )
            },
            mapper = { it!!.toDomain() }
        )
}