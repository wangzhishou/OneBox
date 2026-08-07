package com.shifenmiao.common.handle

import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.model.ListItemType

/**
 * 资源预查询结果。
 */
data class ItemResourceRef(
    val agent: ItemAgentEntity? = null,
    val prompt: PromptEntity? = null,
    val data: ItemDataEntity? = null,
)

/**
 * item → 资源行 的查询工具。
 * - 三种 link 表独立存在；根据 item.listType 直接路由到对应资源表
 * - 调用方传入 AppDatabase
 */
object ItemResourceResolver {

    suspend fun resolve(
        appDatabase: AppDatabase,
        itemId: Int,
        listType: Int?,
    ): ItemResourceRef {
        val type = ListItemType.fromId(listType) ?: return ItemResourceRef()
        return when (type) {
            ListItemType.AGENT -> {
                val linkId = appDatabase.agentDao().getAgentLinkByItemId(itemId)
                ItemResourceRef(agent = linkId?.let { appDatabase.agentDao().getAgentById(it) })
            }
            ListItemType.PROMPT -> {
                val linkId = appDatabase.chatPromptDao().getPromptLinkByItemId(itemId)
                ItemResourceRef(prompt = linkId?.let { appDatabase.chatPromptDao().getPromptById(it) })
            }
            ListItemType.HTML,
            ListItemType.NOTE,
            ListItemType.BLOG -> {
                val linkId = appDatabase.itemDataDao().getDataLinkByItemId(itemId)
                ItemResourceRef(data = linkId?.let { appDatabase.itemDataDao().getById(it) })
            }
            else -> ItemResourceRef()
        }
    }
}
