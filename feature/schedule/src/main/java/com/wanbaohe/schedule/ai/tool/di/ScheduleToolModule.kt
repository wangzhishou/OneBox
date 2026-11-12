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

package com.wanbaohe.schedule.ai.tool.di

import com.shifenmiao.ai.agent.tool.AgentTool
import com.wanbaohe.schedule.ai.tool.AddScheduleEventTool
import com.wanbaohe.schedule.ai.tool.DeleteScheduleEventTool
import com.wanbaohe.schedule.ai.tool.QueryScheduleEventsTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 日程 AI 工具注册模块。
 *
 * 通过 @IntoMap + @StringKey 将日程相关工具注册到 AgentToolRegistry。
 */
@Module
@InstallIn(SingletonComponent::class)
object ScheduleToolModule {

    @Provides
    @IntoMap
    @StringKey("add_schedule_event")
    fun provideAddScheduleEventTool(tool: AddScheduleEventTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("query_schedule_events")
    fun provideQueryScheduleEventsTool(tool: QueryScheduleEventsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("delete_schedule_event")
    fun provideDeleteScheduleEventTool(tool: DeleteScheduleEventTool): AgentTool = tool
}
