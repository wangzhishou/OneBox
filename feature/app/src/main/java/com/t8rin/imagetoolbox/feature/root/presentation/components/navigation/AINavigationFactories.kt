/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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
