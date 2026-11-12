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
import androidx.room.PrimaryKey

/**
 * AI 记忆条目：
 * - kind = [KIND_PROFILE]：用户维护的全局档案（≈ OpenMinis GLOBAL.md），恒定注入，agent 只读；
 * - kind = [KIND_LOG]：agent 经 memory_write 追加的时间序日志，参与注入与关键词搜索。
 */
@Entity(
    tableName = "memory_entry",
    indices = [
        Index(value = ["kind", "enabled", "created_at"]),
    ]
)
data class MemoryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "content")
    val content: String,
    /** 写入来源会话 id，管理页展示与未来"按会话回忆"使用；用户手动新增时为 null。 */
    @ColumnInfo(name = "source_conversation_id")
    val sourceConversationId: String? = null,
    /** 软停用：不注入、不参与搜索。 */
    @ColumnInfo(name = "enabled", defaultValue = "1")
    val enabled: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val KIND_PROFILE = "profile"
        const val KIND_LOG = "log"
    }
}
