package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.UserModel
import com.app.testik.domain.repository.UserRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.google.firebase.firestore.Source
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(uid: String): Result<UserModel> =
        wrap(
            block = { userRepository.getUserInfo(uid = uid, source = Source.DEFAULT) },
            mapper = { it!!.toDomain() }
        )
}