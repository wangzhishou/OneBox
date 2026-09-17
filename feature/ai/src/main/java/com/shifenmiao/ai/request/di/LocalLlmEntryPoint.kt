package com.shifenmiao.ai.request.di

import com.shifenmiao.ai.request.LocalLlmSessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 向 :app 壳模块的 Application 暴露端侧 LLM 会话管理器。
 *
 * Application 在 onTrimMemory 内存压力回调里经 EntryPointAccessors 取 [LocalLlmSessionManager]
 * 并调用 releaseAll() 释放 native 权重,避免进程被系统直接杀掉;
 * 必须走 SessionManager 而不是直接拿 runtime,保证其 loadedModelId 缓存不失真。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocalLlmEntryPoint {
    fun localLlmSessionManager(): LocalLlmSessionManager
}
