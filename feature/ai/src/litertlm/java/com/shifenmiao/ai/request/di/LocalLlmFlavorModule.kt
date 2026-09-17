package com.shifenmiao.ai.request.di

import com.shifenmiao.ai.request.LiteRtLmRuntime
import com.shifenmiao.ai.request.LocalLlmModelRegistry
import com.shifenmiao.ai.request.LocalLlmRuntime
import com.shifenmiao.ai.request.LocalModelDirectoryRegistry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 海外渠道(google/foss)的端侧绑定:LiteRT-LM 真实运行时 + 本地模型目录注册表。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocalLlmFlavorModule {

    @Binds
    @Singleton
    abstract fun bindLocalLlmRuntime(impl: LiteRtLmRuntime): LocalLlmRuntime

    @Binds
    @Singleton
    abstract fun bindLocalLlmModelRegistry(impl: LocalModelDirectoryRegistry): LocalLlmModelRegistry
}
