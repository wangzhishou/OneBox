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

package com.wanbaohe.calendar.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 万年历 AI 工具注册模块。
 *
 * 通过 @IntoMap + @StringKey 将万年历相关工具注册到 AgentToolRegistry。
 */
@Module
@InstallIn(SingletonComponent::class)
object CalendarToolModule {

    @Provides
    @IntoMap
    @StringKey("lunar_calendar_query")
    fun provideLunarCalendarTool(tool: LunarCalendarTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("bazi_calculation")
    fun provideBaZiTool(tool: BaZiTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("auspicious_day_query")
    fun provideAuspiciousDayTool(tool: AuspiciousDayTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("lunar_solar_conversion")
    fun provideLunarConvertTool(tool: LunarConvertTool): AgentTool = tool
}
