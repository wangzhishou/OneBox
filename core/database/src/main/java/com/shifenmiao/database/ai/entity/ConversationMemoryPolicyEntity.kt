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
import androidx.room.PrimaryKey

/**
 * 会话级记忆/技能开关：
 * 主键用 conversation.id（真会话身份），不用工具策略的 scope key——
 * 后者对无 entryRefId 的普通聊天一律退化为 CHAT:default。
 */
@Entity(tableName = "conversation_memory_policy")
data class ConversationMemoryPolicyEntity(
    @PrimaryKey
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    /** 本会话是否注入记忆 + 是否发 memory 工具。 */
    @ColumnInfo(name = "memory_enabled", defaultValue = "1")
    val memoryEnabled: Boolean = true,
    /** 本会话是否注入技能清单 + 是否发 use_skill。 */
    @ColumnInfo(name = "skills_enabled", defaultValue = "1")
    val skillsEnabled: Boolean = true,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
