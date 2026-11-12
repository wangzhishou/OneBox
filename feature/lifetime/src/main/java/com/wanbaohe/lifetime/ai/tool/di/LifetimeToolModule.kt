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

package com.wanbaohe.lifetime.ai.tool.di

import com.shifenmiao.ai.agent.tool.AgentTool
import com.wanbaohe.lifetime.ai.tool.AddCountdownEventTool
import com.wanbaohe.lifetime.ai.tool.AddLifeMilestoneTool
import com.wanbaohe.lifetime.ai.tool.DeleteLifetimeEntryTool
import com.wanbaohe.lifetime.ai.tool.QueryLifeEventsTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 人生时间线 AI 工具注册模块。
 *
 * 通过 @IntoMap + @StringKey 将倒数日/里程碑相关工具注册到 AgentToolRegistry。
 */
@Module
@InstallIn(SingletonComponent::class)
object LifetimeToolModule {

    @Provides
    @IntoMap
    @StringKey("add_countdown_event")
    fun provideAddCountdownEventTool(tool: AddCountdownEventTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("add_life_milestone")
    fun provideAddLifeMilestoneTool(tool: AddLifeMilestoneTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("query_life_events")
    fun provideQueryLifeEventsTool(tool: QueryLifeEventsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("delete_lifetime_entry")
    fun provideDeleteLifetimeEntryTool(tool: DeleteLifetimeEntryTool): AgentTool = tool
}
