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

package com.shifenmiao.ai.service

import com.shifenmiao.ai.agent.tool.ConversationToolPolicyRepository
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ConversationToolPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptTemplateToolService @Inject constructor(
    private val toolBindingRepository: ToolBindingRepository,
    private val conversationToolPolicyRepository: ConversationToolPolicyRepository
) {

    suspend fun getPromptScopedToolNames(promptId: Int?): Set<String>? {
        if (promptId == null || promptId <= 0) return null
        // "未绑定" 语义: 调用方应拿到 null 而非空集, 让上层 fallback 到 entry-scoped / bootstrap.
        return toolBindingRepository.getPromptBoundToolNames(promptId)
    }

    suspend fun ensurePromptDefaultsEnabled(
        conversation: Conversation
    ): Boolean {
        val promptId = conversation.promptId
        if (promptId == null || promptId <= 0) return false

        val existingPolicy = conversationToolPolicyRepository.getPolicy(conversation)
        if (existingPolicy != null) return false

        val defaultTools = toolBindingRepository.getPromptBoundToolNames(promptId)
            ?: return false
        if (defaultTools.isEmpty()) return false

        conversationToolPolicyRepository.savePolicy(
            conversation = conversation,
            policy = ConversationToolPolicy(
                selectedToolNames = defaultTools.toList().sorted()
            )
        )
        return true
    }
}
