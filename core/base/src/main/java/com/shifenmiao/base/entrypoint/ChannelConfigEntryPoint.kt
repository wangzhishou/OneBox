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

package com.shifenmiao.base.entrypoint

import com.shifenmiao.base.channel.ChannelConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 渠道配置 EntryPoint.
 *
 * 业务模块无法直接 @Inject ChannelConfig (因为它在 Composable 里, 不在 HiltComponent 里),
 * 改用 EntryPoint 拿一次:
 *   val cfg = EntryPointAccessors.fromApplication(context, ChannelConfigEntryPoint::class.java).getChannelConfig()
 *
 * 静态 @Inject 的类 (Hilt Component 内部) 直接用 @Inject ChannelConfig 即可.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChannelConfigEntryPoint {
    fun getChannelConfig(): ChannelConfig
}
