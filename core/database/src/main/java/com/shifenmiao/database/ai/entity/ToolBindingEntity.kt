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

package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * 默认工具绑定关系。
 *
 * 统一承接 Agent / Prompt 的默认工具作用域。
 */
@Entity(
    tableName = "tool_binding",
    primaryKeys = ["owner_type", "owner_id", "tool_name"],
    indices = [
        Index(value = ["owner_type", "owner_id"]),
        Index(value = ["tool_name"])
    ]
)
data class ToolBindingEntity(
    @ColumnInfo(name = "owner_type")
    val ownerType: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: Int,
    @ColumnInfo(name = "tool_name")
    val toolName: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int
) {
    object OwnerType {
        const val AGENT = "AGENT"
        const val PROMPT = "PROMPT"
        // CHAT / ASSISTANT 旧 owner_type 已废弃 (首轮默认统一走 bootstrap 兜底),
        // 常量一并删除. 旧行由 AppDatabase.MIGRATION_2_3 清理.
    }
}
