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
