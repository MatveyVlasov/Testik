package com.app.tests.domain.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.RegistrationDto

interface FirestoreRepository {

    suspend fun addUser(data: RegistrationDto): ApiResult<Unit>
}