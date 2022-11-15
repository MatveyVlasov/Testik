package com.app.testik.di

import com.app.testik.data.repository.AuthRepositoryImpl
import com.app.testik.data.repository.FirestoreRepositoryImpl
import com.app.testik.data.repository.PreferencesRepositoryImpl
import com.app.testik.data.repository.StorageRepositoryImpl
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.domain.repository.FirestoreRepository
import com.app.testik.domain.repository.PreferencesRepository
import com.app.testik.domain.repository.StorageRepository
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