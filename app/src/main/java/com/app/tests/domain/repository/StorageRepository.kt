package com.app.tests.domain.repository

import android.net.Uri
import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.UserDto

interface StorageRepository {

    suspend fun uploadAvatar(data: UserDto): ApiResult<Uri>
}