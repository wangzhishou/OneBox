package com.wanbaohe.habittracker.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 习惯打卡 AI 工具注册模块。
 *
 * 通过 @IntoMap + @StringKey 将习惯相关工具注册到 AgentToolRegistry。
 */
@Module
@InstallIn(SingletonComponent::class)
object HabitToolModule {

    @Provides
    @IntoMap
    @StringKey("add_habit")
    fun provideAddHabitTool(tool: AddHabitTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("check_in_habit")
    fun provideCheckInHabitTool(tool: CheckInHabitTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("query_habits")
    fun provideQueryHabitsTool(tool: QueryHabitsTool): AgentTool = tool
}
