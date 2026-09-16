package com.shifenmiao.ai.request.di

import com.shifenmiao.ai.request.EmptyLocalLlmModelRegistry
import com.shifenmiao.ai.request.LocalLlmModelRegistry
import com.shifenmiao.ai.request.LocalLlmRuntime
import com.shifenmiao.ai.request.UnsupportedLocalLlmRuntime
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 国内渠道的端侧绑定:降级运行时(prepare 直接失败)+ 空注册表。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocalLlmFlavorModule {

    @Binds
    @Singleton
    abstract fun bindLocalLlmRuntime(impl: UnsupportedLocalLlmRuntime): LocalLlmRuntime

    @Binds
    @Singleton
    abstract fun bindLocalLlmModelRegistry(impl: EmptyLocalLlmModelRegistry): LocalLlmModelRegistry
}
