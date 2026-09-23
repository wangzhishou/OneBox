package com.wanbaohe.chess.di

import com.wanbaohe.chess.application.usecase.CreateGameUseCase
import com.wanbaohe.chess.application.usecase.OnlinePlayUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing online-play use cases from UI composables.
 * 方法名带 chess 前缀:与象棋/五子棋的同名 EntryPoint getter 在 Hilt 组件上冲突。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChessOnlineEntryPoint {
    fun chessOnlinePlayUseCase(): OnlinePlayUseCase
    fun chessCreateGameUseCase(): CreateGameUseCase
}
