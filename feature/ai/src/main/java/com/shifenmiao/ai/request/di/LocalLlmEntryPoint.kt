package com.shifenmiao.ai.request.di

import com.shifenmiao.ai.request.LocalLlmRuntime
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 向 :app 壳模块的 Application 暴露端侧 LLM 运行时。
 *
 * Application 在 onTrimMemory 内存压力回调里经 EntryPointAccessors 取 [LocalLlmRuntime]
 * 并调用 releaseAll() 释放 native 权重,避免进程被系统直接杀掉。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocalLlmEntryPoint {
    fun localLlmRuntime(): LocalLlmRuntime
}
