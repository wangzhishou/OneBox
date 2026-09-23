package com.wanbaohe.gomoku.di

import com.wanbaohe.gomoku.application.usecase.CreateGameUseCase
import com.wanbaohe.gomoku.application.usecase.OnlinePlayUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing online-play use cases from UI composables.
 * 方法名带 gomoku 前缀:与象棋/国际象棋的同名 EntryPoint getter 在 Hilt 组件上冲突。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface GomokuOnlineEntryPoint {
    fun gomokuOnlinePlayUseCase(): OnlinePlayUseCase
    fun gomokuCreateGameUseCase(): CreateGameUseCase
}
