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

package com.shifenmiao.ai.agent.tool.di

import com.shifenmiao.ai.agent.tool.AgentTool
import dagger.Module
import dagger.multibindings.Multibinds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt 多绑定基线模块 —— 声明空的 AgentTool Map。
 *
 * 各 feature 模块通过 @IntoMap + @StringKey 向此 Map 注入工具，
 * 无需修改 AI 模块代码即可扩展工具集。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AgentToolModule {

    @Multibinds
    abstract fun bindAgentToolMap(): Map<String, AgentTool>
}

