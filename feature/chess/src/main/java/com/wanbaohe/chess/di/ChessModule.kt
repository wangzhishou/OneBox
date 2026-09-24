package com.wanbaohe.chess.di

import com.wanbaohe.chess.application.port.outbound.AiTaskStore
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.application.port.outbound.MoveChooser
import com.wanbaohe.chess.application.port.outbound.MoveStore
import com.wanbaohe.chess.application.port.outbound.SettingsStore
import com.wanbaohe.chess.application.port.outbound.SoundPlayer
import com.wanbaohe.chess.application.port.outbound.TtsEngine
import com.wanbaohe.chess.application.port.outbound.ChessAiStore
import com.wanbaohe.chess.data.AiTaskDaoAdapter
import com.wanbaohe.chess.data.AudioAdapter
import com.wanbaohe.chess.data.DispatchingMoveChooser
import com.wanbaohe.chess.data.GameDaoAdapter
import com.wanbaohe.chess.data.PlyDaoAdapter
import com.wanbaohe.chess.data.SettingsPrefsAdapter
import com.wanbaohe.chess.data.TtsAdapter
import com.wanbaohe.chess.data.ChessAiPrefsAdapter
import com.wanbaohe.chess.data.online.SignalingApi
import com.wanbaohe.chess.data.online.SignalingClientImpl
import com.wanbaohe.chess.application.port.outbound.SignalingClient
import com.wanbaohe.chess.service.ChessServiceImpl
import com.shifenmiao.model.chess.ChessServiceInterface
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
abstract class ChessModule {

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
    abstract fun bindChessAiStore(adapter: ChessAiPrefsAdapter): ChessAiStore

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
    abstract fun bindChessService(impl: ChessServiceImpl): ChessServiceInterface

    companion object {
        @Provides
        @Singleton
        fun provideSignalingApi(@Named("DefaultRetrofit") retrofit: Retrofit): SignalingApi =
            retrofit.create(SignalingApi::class.java)
    }
}
