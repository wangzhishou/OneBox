package com.wanbaohe.teleprompter.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 提词器 AI 工具注册模块。
 *
 * 通过 @IntoMap + @StringKey 将提词器相关工具注册到 AgentToolRegistry。
 */
@Module
@InstallIn(SingletonComponent::class)
object TeleprompterToolModule {

    @Provides
    @IntoMap
    @StringKey("manage_teleprompter")
    fun provideManageTeleprompterTool(tool: ManageTeleprompterTool): AgentTool = tool
}
