package com.shifenmiao.ai.component.di

import com.shifenmiao.ai.component.AgentLoopBillingObserver
import com.shifenmiao.ai.component.AgentLoopInterceptor
import com.shifenmiao.ai.component.AgentLoopLoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * AgentLoopInterceptor 多绑定模块 —— 通过 @IntoSet 注册循环拦截器。
 *
 * 新增拦截器时在此追加一个 @Provides @IntoSet 即可,
 * AgentLoopRunner / AgentLoopExecutor 注入的 Set<AgentLoopInterceptor> 自动收齐。
 */
@Module
@InstallIn(SingletonComponent::class)
object AgentLoopInterceptorModule {

    @Provides
    @IntoSet
    fun provideLoggingInterceptor(impl: AgentLoopLoggingInterceptor): AgentLoopInterceptor = impl

    @Provides
    @IntoSet
    fun provideBillingObserver(impl: AgentLoopBillingObserver): AgentLoopInterceptor = impl
}
