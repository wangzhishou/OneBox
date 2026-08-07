package com.shifenmiao.ai.di

import com.shifenmiao.ai.service.AgentFileServiceImpl
import com.shifenmiao.model.file.AgentFileService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiFileModule {

    @Binds
    @Singleton
    abstract fun bindAgentFileService(
        impl: AgentFileServiceImpl,
    ): AgentFileService
}

