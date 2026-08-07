package com.wanbaohe.passwordvault.di

import com.wanbaohe.passwordvault.service.PasswordVaultService
import com.wanbaohe.passwordvault.service.PasswordVaultServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PasswordVaultModule {

    @Binds
    @Singleton
    abstract fun bindPasswordVaultService(
        impl: PasswordVaultServiceImpl
    ): PasswordVaultService
}
