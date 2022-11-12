package com.app.tests.di

import com.app.tests.data.repository.AuthRepositoryImpl
import com.app.tests.data.repository.FirestoreRepositoryImpl
import com.app.tests.data.repository.PreferencesRepositoryImpl
import com.app.tests.data.repository.StorageRepositoryImpl
import com.app.tests.domain.repository.AuthRepository
import com.app.tests.domain.repository.FirestoreRepository
import com.app.tests.domain.repository.PreferencesRepository
import com.app.tests.domain.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(preferencesRepositoryImpl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFirestoreRepository(firestoreRepositoryImpl: FirestoreRepositoryImpl): FirestoreRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(storageRepositoryImpl: StorageRepositoryImpl): StorageRepository
}