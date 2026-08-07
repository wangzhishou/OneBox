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

