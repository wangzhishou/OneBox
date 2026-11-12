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
import java.util.Date

/**
 * 列表用：item + categories + clickStat + userState。
 *
 * 不再携带 agent / prompt / data 大资源，详情页才走 ItemWithRelation。
 */
data class ItemWithCategoriesAndStats(
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
        entityColumn = "item_id"
    )
    var clickStat: ItemClickStatEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    var userState: ItemUserState? = null,
) {
    val clickCount: Int
        get() = clickStat?.clickCount ?: 0

    val clickTime: Date
        get() = clickStat?.clickTimeAsDate ?: EPOCH_TIME

    val isFavorited: Boolean
        get() = userState?.isFavorited == true

    val isPinned: Boolean
        get() = userState?.isPinned == true

    val requiresAuth: Boolean
        get() = userState?.requiresAuth == true

    fun toItemWithCategories(): ItemWithCategories = ItemWithCategories(
        item = item,
        categories = categories,
        userState = userState,
    )

    private companion object {
        val EPOCH_TIME = Date(0)
    }
}
