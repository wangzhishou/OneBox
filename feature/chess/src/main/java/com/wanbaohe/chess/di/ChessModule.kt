/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
