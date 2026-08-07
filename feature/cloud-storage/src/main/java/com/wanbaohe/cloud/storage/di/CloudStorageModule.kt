package com.wanbaohe.cloud.storage.di

import com.wanbaohe.cloud.storage.data.CloudStorageRepository
import com.wanbaohe.cloud.storage.data.CloudStorageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CloudStorageModule {

    @Binds
    @Singleton
    fun bindCloudStorageRepository(
        repository: CloudStorageRepositoryImpl
    ): CloudStorageRepository
}
