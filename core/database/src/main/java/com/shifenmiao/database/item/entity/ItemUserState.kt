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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * item 的本地用户态（收藏 / 置顶 / 可编辑）。
 *
 * 拆出独立表的收益：
 * 1. 同步 item 服务端字段时不再需要保护 N 个本地列
 * 2. "显示已收藏" 等过滤查询走小表的索引
 * 3. 服务端下架 item 时的 CASCADE 不再误删用户标记外的字段
 *
 * 与 item 是 1:1 关系，由 [itemId] PK 强制。
 */
@Entity(
    tableName = "item_user_state",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["is_favorited"]),
        Index(value = ["is_pinned", "pinned_at"]),
    ]
)
data class ItemUserState(
    @PrimaryKey
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "is_favorited", defaultValue = "0") val isFavorited: Boolean = false,
    @ColumnInfo(name = "is_pinned", defaultValue = "0") val isPinned: Boolean = false,
    @ColumnInfo(name = "pinned_at") val pinnedAt: Long? = null,
    @ColumnInfo(name = "can_edit", defaultValue = "0") val canEdit: Boolean = false,
    @ColumnInfo(name = "requires_auth", defaultValue = "0") val requiresAuth: Boolean = false,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
