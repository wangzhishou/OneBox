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
import androidx.room.Junction
import androidx.room.Relation

/**
 * Lightweight version of ItemWithRelation for list display.
 * Does NOT include agent and prompt relations to reduce memory usage.
 * Use ItemWithRelation only when navigating to detail pages where agent/prompt data is needed.
 *
 * 携带 userState 是为了让 [com.shifenmiao.common.handle.HandleEvent] 等中间层
 * 能直接读 requiresAuth / isFavorited 等本地用户态字段,避免每个调用点重复 DAO 查询。
 */
data class ItemWithCategories(
    @Embedded var item: ItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemCategoryCrossRef::class,
            parentColumn = "item_id",
            entityColumn = "category_id"
        )
    )
    var categories: List<Category> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    var userState: ItemUserState? = null,
)

val ItemWithCategories.requiresAuth: Boolean
    get() = userState?.requiresAuth == true

