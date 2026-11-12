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
import com.shifenmiao.database.chat_prompt.entity.PromptEntity

/**
 * item ↔ prompt 资源 1:1 关联。
 */
@Entity(
    tableName = "item_prompt_link",
    primaryKeys = ["item_id"],
    foreignKeys = [
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["item_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PromptEntity::class, parentColumns = ["id"], childColumns = ["prompt_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["prompt_id"], unique = true),
    ]
)
data class ItemPromptLink(
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "prompt_id") val promptId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
