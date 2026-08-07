package com.wanbaohe.xiangqi.di

import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStore
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveStore
import com.wanbaohe.xiangqi.application.port.outbound.SettingsStore
import com.wanbaohe.xiangqi.application.port.outbound.SignalingClient
import com.wanbaohe.xiangqi.application.port.outbound.SoundPlayer
import com.wanbaohe.xiangqi.application.port.outbound.TtsEngine
import com.wanbaohe.xiangqi.data.AiTaskDaoAdapter
import com.wanbaohe.xiangqi.data.AudioAdapter
import com.wanbaohe.xiangqi.data.GameDaoAdapter
import com.wanbaohe.xiangqi.data.LlmMoveChooser
import com.wanbaohe.xiangqi.data.PlyDaoAdapter
import com.wanbaohe.xiangqi.data.SettingsPrefsAdapter
import com.wanbaohe.xiangqi.data.TtsAdapter
import com.wanbaohe.xiangqi.data.online.SignalingApi
import com.wanbaohe.xiangqi.data.online.SignalingClientImpl
import com.shifenmiao.model.xiangqi.XiangqiServiceInterface
import com.wanbaohe.xiangqi.service.XiangqiServiceImpl
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
abstract class XiangqiModule {

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
    abstract fun bindMoveChooser(adapter: LlmMoveChooser): MoveChooser

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
    abstract fun bindXiangqiService(impl: XiangqiServiceImpl): XiangqiServiceInterface

    companion object {
        @Provides
        @Singleton
        fun provideSignalingApi(@Named("DefaultRetrofit") retrofit: Retrofit): SignalingApi =
            retrofit.create(SignalingApi::class.java)
    }
}
