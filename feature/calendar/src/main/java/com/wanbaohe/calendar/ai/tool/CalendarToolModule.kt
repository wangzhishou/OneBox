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
