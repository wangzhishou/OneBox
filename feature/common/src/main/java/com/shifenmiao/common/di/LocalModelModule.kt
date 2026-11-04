package com.shifenmiao.common.di

import com.shifenmiao.common.localmodel.DefaultLocalModelRepository
import com.shifenmiao.common.localmodel.LocalModelRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalModelModule {
    @Binds
    @Singleton
    abstract fun bindLocalModelRepository(
        impl: DefaultLocalModelRepository,
    ): LocalModelRepository
}
