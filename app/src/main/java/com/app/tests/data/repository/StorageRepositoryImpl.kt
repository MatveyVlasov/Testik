package com.app.tests.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.UserDto
import com.app.tests.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadAvatar(data: UserDto): ApiResult<Uri> {
        return try {
            val uri = File(data.avatar).toUri()
            val avatar = firebaseStorage.reference.child("avatars").child(data.email)
                .putFile(uri).await()
                .storage.downloadUrl.await()
            ApiResult.Success(avatar)
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}