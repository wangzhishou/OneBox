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

import com.shifenmiao.database.ai.dao.ToolBindingDao
import com.shifenmiao.database.ai.entity.ToolBindingEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 工具绑定仓库 —— 持久化 AGENT / PROMPT 维度的"默认开启"集合.
 *
 * 历史上的 CHAT / ASSISTANT 绑定已废弃: 这两类入口的首轮默认走
 * [com.shifenmiao.ai.agent.tool.AgentTool] 的
 * [com.shifenmiao.model.ai.tool.ToolCatalogItem.bootstrap] 兜底, 不再依赖本表.
 * 旧数据保留在 [ToolBindingDao] 的 CHAT / ASSISTANT 行里, 不会被读取.
 */
@Singleton
class ToolBindingRepository @Inject constructor(
    private val toolBindingDao: ToolBindingDao
) {
    suspend fun getAgentBoundToolNames(agentId: Int): Set<String>? {
        return getBoundToolNames(
            ownerType = ToolBindingEntity.OwnerType.AGENT,
            ownerId = agentId
        )
    }

    suspend fun getPromptBoundToolNames(promptId: Int): Set<String>? {
        return getBoundToolNames(
            ownerType = ToolBindingEntity.OwnerType.PROMPT,
            ownerId = promptId
        )
    }

    suspend fun replaceAgentBindings(agentId: Int, toolNames: List<String>) {
        replaceBindings(
            ownerType = ToolBindingEntity.OwnerType.AGENT,
            ownerId = agentId,
            toolNames = toolNames
        )
    }

    suspend fun replacePromptBindings(promptId: Int, toolNames: List<String>) {
        replaceBindings(
            ownerType = ToolBindingEntity.OwnerType.PROMPT,
            ownerId = promptId,
            toolNames = toolNames
        )
    }

    private suspend fun getBoundToolNames(ownerType: String, ownerId: Int): Set<String>? {
        val names = toolBindingDao.getToolNames(ownerType = ownerType, ownerId = ownerId)
        return names.takeIf { it.isNotEmpty() }?.toSet()
    }

    private suspend fun replaceBindings(
        ownerType: String,
        ownerId: Int,
        toolNames: List<String>
    ) {
        toolBindingDao.deleteByOwner(ownerType = ownerType, ownerId = ownerId)
        if (toolNames.isEmpty()) return

        val bindings = toolNames.distinct().mapIndexed { index, toolName ->
            ToolBindingEntity(
                ownerType = ownerType,
                ownerId = ownerId,
                toolName = toolName,
                sortOrder = index
            )
        }
        toolBindingDao.insertAll(bindings)
    }
}
