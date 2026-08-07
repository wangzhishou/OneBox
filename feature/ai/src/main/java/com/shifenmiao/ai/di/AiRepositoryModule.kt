package com.shifenmiao.ai.di

import com.shifenmiao.ai.repository.ConversationRepository
import com.shifenmiao.ai.repository.DefaultConversationRepository
import com.shifenmiao.ai.repository.DefaultMessageRepository
import com.shifenmiao.ai.repository.DefaultPromptRepository
import com.shifenmiao.ai.repository.MessageRepository
import com.shifenmiao.ai.repository.PromptRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPromptRepository(
        impl: DefaultPromptRepository,
    ): PromptRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: DefaultConversationRepository,
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: DefaultMessageRepository,
    ): MessageRepository
}

