package com.app.tests.domain.usecase

import com.app.tests.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): FirebaseUser? = authRepository.getCurrentUser()
}