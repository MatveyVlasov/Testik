package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.Result
import com.app.testik.domain.model.UserModel
import com.app.testik.domain.repository.UserRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(query: String): Result<List<UserModel>> =
        wrap(
            block = { userRepository.getUsers(query = query) },
            mapper = { it!!.map { user -> user.toDomain() } }
        )
}