package com.wanbaohe.xiangqi.di

import com.wanbaohe.xiangqi.application.usecase.CreateGameUseCase
import com.wanbaohe.xiangqi.application.usecase.OnlinePlayUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing online-play use cases from UI composables.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface XiangqiOnlineEntryPoint {
    fun onlinePlayUseCase(): OnlinePlayUseCase
    fun createGameUseCase(): CreateGameUseCase
}
