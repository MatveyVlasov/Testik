package com.app.testik.domain.repository

import android.net.Uri
import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.UserDto

interface StorageRepository {

    suspend fun uploadAvatar(data: UserDto): ApiResult<Uri>

    suspend fun deleteAvatar(email: String): ApiResult<Unit>

    suspend fun uploadTestImage(testId: String, image: String): ApiResult<Uri>

    suspend fun deleteTestImage(testId: String): ApiResult<Unit>
}