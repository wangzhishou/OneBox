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

package com.wanbaohe.xiangqi.router

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.common.ui.ImmersiveModeState

/**
 * 提供给象棋对局子屏幕的沉浸式模式状态。
 * Router 顶层创建,Game/Analysis Screen 可通过该 CompositionLocal toggle。
 */
val LocalXiangqiImmersiveModeState = compositionLocalOf<ImmersiveModeState?> { null }
