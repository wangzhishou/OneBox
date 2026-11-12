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

package com.shifenmiao.ai.memory

import com.shifenmiao.database.ai.dao.ConversationMemoryPolicyDao
import com.shifenmiao.database.ai.entity.ConversationMemoryPolicyEntity
import com.shifenmiao.storage.AIChatStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 会话级记忆/技能开关仓库。
 *
 * 主键用 conversation.id（真会话身份），不用工具策略的 scope key——
 * 后者对无 entryRefId 的普通聊天一律退化为 CHAT:default。
 * 无策略行 = 默认全开（true）。
 *
 * "全局 MMKV AND 会话表"的门控判定收拢在本仓库
 * （[isMemoryEnabledFor] / [isSkillsEnabledFor]），
 * ToolConfigResolver（prompt 组装）与 AgentToolRegistry（执行层）共用同一份规则。
 */
@Singleton
class ConversationMemoryPolicyRepository @Inject constructor(
    private val dao: ConversationMemoryPolicyDao
) {

    suspend fun getPolicy(conversationId: String): ConversationMemoryPolicyEntity? {
        if (conversationId.isBlank()) return null
        return dao.getByConversationId(conversationId)
    }

    fun observePolicy(conversationId: String): Flow<ConversationMemoryPolicyEntity?> {
        return dao.observeByConversationId(conversationId)
    }

    /** 记忆门控：全局 MMKV AND 会话表；conversationId 空白（非会话链路/草稿态）视为 true 不设防 */
    suspend fun isMemoryEnabledFor(conversationId: String?): Boolean {
        return AIChatStorage.isEnableMemory.value &&
            isSessionFlagOn(conversationId) { it.memoryEnabled }
    }

    /** 技能门控：规则同 [isMemoryEnabledFor] */
    suspend fun isSkillsEnabledFor(conversationId: String?): Boolean {
        return AIChatStorage.isEnableSkills.value &&
            isSessionFlagOn(conversationId) { it.skillsEnabled }
    }

    private suspend fun isSessionFlagOn(
        conversationId: String?,
        flag: (ConversationMemoryPolicyEntity) -> Boolean
    ): Boolean {
        if (conversationId.isNullOrBlank()) return true
        return dao.getByConversationId(conversationId)?.let(flag) != false
    }

    suspend fun setMemoryEnabled(conversationId: String, enabled: Boolean) {
        if (conversationId.isBlank()) return
        // 先初始化行（不存在则插入默认值行），再字段级更新，消除 read-modify-write 竞态
        dao.insertIgnore(ConversationMemoryPolicyEntity(conversationId = conversationId))
        dao.setMemoryEnabled(conversationId, enabled, System.currentTimeMillis())
    }

    suspend fun setSkillsEnabled(conversationId: String, enabled: Boolean) {
        if (conversationId.isBlank()) return
        dao.insertIgnore(ConversationMemoryPolicyEntity(conversationId = conversationId))
        dao.setSkillsEnabled(conversationId, enabled, System.currentTimeMillis())
    }
}
