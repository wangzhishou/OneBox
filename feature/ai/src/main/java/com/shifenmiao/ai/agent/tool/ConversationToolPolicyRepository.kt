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

package com.shifenmiao.ai.agent.tool

import com.shifenmiao.database.ai.dao.ConversationToolPolicyDao
import com.shifenmiao.database.ai.entity.ConversationToolPolicyEntity
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ConversationToolPolicy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationToolPolicyRepository @Inject constructor(
    private val dao: ConversationToolPolicyDao
) {

    suspend fun getPolicy(conversation: Conversation): ConversationToolPolicy? {
        val scopeKey = buildScopeKey(conversation)
        dao.getByConversationId(scopeKey)?.toModel()?.let { return it.normalize() }

        val legacyConversationId = conversation.id
        if (legacyConversationId.isBlank() || legacyConversationId == scopeKey) return null

        val legacyPolicy = dao.getByConversationId(legacyConversationId)?.toModel()?.normalize() ?: return null
        savePolicy(conversation, legacyPolicy)
        return legacyPolicy
    }

    suspend fun savePolicy(conversation: Conversation, policy: ConversationToolPolicy) {
        dao.upsert(
            ConversationToolPolicyEntity(
                conversationId = buildScopeKey(conversation),
                enabledToolNamesJson = AppJson.encodeToString(policy.normalize())
            )
        )
    }

    private fun ConversationToolPolicyEntity.toModel(): ConversationToolPolicy {
        if (enabledToolNamesJson.isBlank()) return ConversationToolPolicy()
        runCatching {
            AppJson.decodeFromString<ConversationToolPolicy>(enabledToolNamesJson)
        }.getOrNull()?.let { return it.normalize() }

        return ConversationToolPolicy(
            selectedToolNames = decodeStringList(enabledToolNamesJson)
        ).normalize()
    }

    private fun ConversationToolPolicy.normalize(): ConversationToolPolicy {
        return copy(
            selectedToolNames = selectedToolNames
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
        )
    }

    private fun buildScopeKey(conversation: Conversation): String {
        val scopeRef = when {
            !conversation.entryRefId.isNullOrBlank() -> conversation.entryRefId!!.trim()
            conversation.entryType == AIConversationEntryType.PROMPT && conversation.promptId != null -> conversation.promptId.toString()
            else -> "default"
        }
        return "${conversation.entryType.name}:$scopeRef"
    }

    private fun decodeStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return try {
            AppJson.decodeFromString<List<String>>(json)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
