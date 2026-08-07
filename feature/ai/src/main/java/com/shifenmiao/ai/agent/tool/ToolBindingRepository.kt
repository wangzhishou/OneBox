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
 * [com.shifenmiao.model.ai.tool.ToolCatalogItem.bootstrapModes] 兜底, 不再依赖本表.
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
