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

package com.shifenmiao.database.item.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity

/**
 * item + 关联表（categories / agent / prompt / data / userState）。
 *
 * 所有资源都通过 Junction + @Relation 自动加载，调用方不再需要手动 fill。
 */
data class ItemWithRelation(
    @Embedded var item: ItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ItemCategoryCrossRef::class,
            parentColumn = "item_id",
            entityColumn = "category_id"
        )
    )
    var categories: List<Category> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ItemAgentLink::class,
            parentColumn = "item_id",
            entityColumn = "agent_id"
        )
    )
    var agent: ItemAgentEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ItemPromptLink::class,
            parentColumn = "item_id",
            entityColumn = "prompt_id"
        )
    )
    var prompt: PromptEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ItemDataLink::class,
            parentColumn = "item_id",
            entityColumn = "data_id"
        )
    )
    var data: ItemDataEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    var userState: ItemUserState? = null,
)
