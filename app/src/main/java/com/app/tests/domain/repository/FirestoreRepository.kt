package com.app.tests.domain.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.RegistrationDto
import com.app.tests.data.model.UserDto

interface FirestoreRepository {

    suspend fun addUser(data: RegistrationDto): ApiResult<Unit>

    suspend fun getUserInfo(email: String?): ApiResult<UserDto>
}