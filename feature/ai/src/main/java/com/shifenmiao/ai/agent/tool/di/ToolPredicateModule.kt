package com.shifenmiao.ai.agent.tool.di

import com.shifenmiao.ai.agent.tool.ToolPredicate
import com.shifenmiao.ai.agent.tool.predicate.ProtocolToolPredicate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * ToolPredicate 多绑定模块 —— 通过 @IntoSet 注册谓词。
 *
 * 新增筛选规则时在此追加一个 @Provides @IntoSet 即可，
 * AgentLoopOrchestrator 注入的 Set<ToolPredicate> 自动收齐。
 */
@Module
@InstallIn(SingletonComponent::class)
object ToolPredicateModule {

    @Provides
    @IntoSet
    fun provideProtocolToolPredicate(impl: ProtocolToolPredicate): ToolPredicate = impl
}
