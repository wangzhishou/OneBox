package com.wanbaohe.gomoku.di

import com.wanbaohe.gomoku.application.port.outbound.AiTaskStore
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.MoveChooser
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.application.port.outbound.SettingsStore
import com.wanbaohe.gomoku.application.port.outbound.SignalingClient
import com.wanbaohe.gomoku.application.port.outbound.SoundPlayer
import com.wanbaohe.gomoku.application.port.outbound.TtsEngine
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiStore
import com.wanbaohe.gomoku.data.AiTaskDaoAdapter
import com.wanbaohe.gomoku.data.AudioAdapter
import com.wanbaohe.gomoku.data.DispatchingMoveChooser
import com.wanbaohe.gomoku.data.GameDaoAdapter
import com.wanbaohe.gomoku.data.PlyDaoAdapter
import com.wanbaohe.gomoku.data.SettingsPrefsAdapter
import com.wanbaohe.gomoku.data.TtsAdapter
import com.wanbaohe.gomoku.data.GomokuAiPrefsAdapter
import com.wanbaohe.gomoku.data.online.SignalingApi
import com.wanbaohe.gomoku.data.online.SignalingClientImpl
import com.wanbaohe.gomoku.service.GomokuServiceImpl
import com.shifenmiao.model.gomoku.GomokuServiceInterface
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GomokuModule {

    @Binds
    @Singleton
    abstract fun bindGameStore(adapter: GameDaoAdapter): GameStore

    @Binds
    @Singleton
    abstract fun bindMoveStore(adapter: PlyDaoAdapter): MoveStore

    @Binds
    @Singleton
    abstract fun bindAiTaskStore(adapter: AiTaskDaoAdapter): AiTaskStore

    @Binds
    @Singleton
    abstract fun bindSettingsStore(adapter: SettingsPrefsAdapter): SettingsStore

    @Binds
    @Singleton
    abstract fun bindGomokuAiStore(adapter: GomokuAiPrefsAdapter): GomokuAiStore

    @Binds
    @Singleton
    abstract fun bindMoveChooser(adapter: DispatchingMoveChooser): MoveChooser

    @Binds
    @Singleton
    abstract fun bindSoundPlayer(adapter: AudioAdapter): SoundPlayer

    @Binds
    @Singleton
    abstract fun bindTtsEngine(adapter: TtsAdapter): TtsEngine

    @Binds
    @Singleton
    abstract fun bindSignalingClient(impl: SignalingClientImpl): SignalingClient

    @Binds
    @Singleton
    abstract fun bindGomokuService(impl: GomokuServiceImpl): GomokuServiceInterface

    companion object {
        @Provides
        @Singleton
        fun provideSignalingApi(@Named("DefaultRetrofit") retrofit: Retrofit): SignalingApi =
            retrofit.create(SignalingApi::class.java)
    }
}
