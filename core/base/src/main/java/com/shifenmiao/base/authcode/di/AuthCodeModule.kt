package com.shifenmiao.base.authcode.di

import com.shifenmiao.base.authcode.AuthCodeAuthCodeVerifier
import com.shifenmiao.base.authcode.AuthCodeService
import com.shifenmiao.base.authcode.AuthCodeServiceImpl
import com.shifenmiao.model.auth.AuthCodeVerifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthCodeModule {

    @Binds
    @Singleton
    abstract fun bindAuthCodeService(impl: AuthCodeServiceImpl): AuthCodeService

    @Binds
    @Singleton
    abstract fun bindAuthCodeVerifier(impl: AuthCodeAuthCodeVerifier): AuthCodeVerifier
}
