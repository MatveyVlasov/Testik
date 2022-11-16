package com.app.testik.domain.repository

import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.RegistrationDto
import com.app.testik.data.model.UserDto
import com.google.firebase.firestore.Source

interface UserRepository {

    suspend fun addUser(data: RegistrationDto): ApiResult<Unit>

    suspend fun getUserInfo(email: String?, source: Source): ApiResult<UserDto>

    suspend fun updateUser(data: UserDto): ApiResult<Unit>

    suspend fun deleteUser(email: String?): ApiResult<Unit>
}