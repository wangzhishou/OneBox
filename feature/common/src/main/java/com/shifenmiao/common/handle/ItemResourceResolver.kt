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
