package com.app.tests.domain.usecase

import com.app.tests.domain.mapper.toDomain
import com.app.tests.domain.model.Result
import com.app.tests.domain.model.UserModel
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.repository.FirestoreRepository
import com.app.tests.domain.util.ResultWrapper
import com.app.tests.domain.util.ResultWrapperImpl
import com.google.firebase.firestore.Source
import javax.inject.Inject

class GetCurrentUserInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(source: Source = Source.DEFAULT): Result<UserModel> =
        wrap(
            block = {
                firestoreRepository.getUserInfo(
                    email = authRepository.getCurrentUser()?.email,
                    source = source
                )
            },
            mapper = { it!!.toDomain() }
        )
}