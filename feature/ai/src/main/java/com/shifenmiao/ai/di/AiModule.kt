package com.shifenmiao.ai.di

import com.google.gson.Gson
import com.shifenmiao.ai.file.AppWorkspaceResolver
import com.shifenmiao.ai.prompt.AndroidEnvironmentContextProvider
import com.shifenmiao.ai.prompt.EnvironmentContextProvider
import com.shifenmiao.model.ModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * feature/ai 模块的 Hilt 依赖提供。
 *
 * 全局共享 Gson 实例，避免在热路径（流式聊天、Agent Loop、工具调用）中
 * 重复创建 Gson（内部构建大量 TypeAdapter，构造开销高）。
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = ModelProvider.provideGson()

    @Provides
    @Singleton
    fun provideEnvironmentContextProvider(
        appWorkspaceResolver: AppWorkspaceResolver,
    ): EnvironmentContextProvider = AndroidEnvironmentContextProvider(appWorkspaceResolver)
}
