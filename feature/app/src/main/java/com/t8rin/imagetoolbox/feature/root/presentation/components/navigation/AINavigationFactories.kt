package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import javax.inject.Inject

import com.shifenmiao.ai.component.AIDuelChatComponent
import com.shifenmiao.ai.component.AIHistoryCenterComponent
import com.shifenmiao.ai.component.AgentJsonEditorComponent
import com.shifenmiao.ai.component.CreateAIAgentComponent
import com.shifenmiao.ai.component.CreateAIPromptComponent
import com.shifenmiao.ai.component.TokenUsageComponent
import com.shifenmiao.ai.image.controllers.AIImageComponent

/**
 * 启动期按需解析的工厂集合——AI 组。
 *
 * 由 [ChildProvider] 通过 `Provider<AINavigationFactories>` 持有，
 * 仅当用户首次进入本组对应 screen 时才触发 Hilt 解析本类字段，
 * 避免冷启时一次性解析全部 110 个 binding 节点。
 */
class AINavigationFactories @Inject constructor(
    val aiDuelChatComponentFactory: AIDuelChatComponent.Factory,
    val aiHistoryCenterComponentFactory: AIHistoryCenterComponent.Factory,
    val aiImageComponentFactory: AIImageComponent.Factory,
    val aiStreamAnswerComponentFactory: com.shifenmiao.ai.component.AIStreamAnswerComponent.Factory,
    val createAIAgentComponentFactory: CreateAIAgentComponent.Factory,
    val createAIPromptComponentFactory: CreateAIPromptComponent.Factory,
    val agentJsonEditorComponentFactory: AgentJsonEditorComponent.Factory,
    val tokenUsageComponentFactory: TokenUsageComponent.Factory,
)
