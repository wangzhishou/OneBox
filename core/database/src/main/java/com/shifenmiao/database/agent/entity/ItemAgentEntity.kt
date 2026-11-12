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

package com.shifenmiao.database.agent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.Source

/**
 * AGENT 类型条目的资源行。
 * 通过 [com.shifenmiao.database.item.entity.ItemAgentLink] 与 item 建立 1:1 关联。
 */
@Entity(
    tableName = "item_agent",
    indices = [
        Index(value = ["source", "document_id"], unique = true),
    ]
)
data class ItemAgentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "remote_id") val remoteId: Int? = null,
    /**
     * Strapi v5 文档级 cuid，远端 agent 的同步去重主键；数字 [remoteId] 重发后会漂移，仅作遗留信息。
     * 本地用户 agent 没有该字段，保持 null。
     */
    @ColumnInfo(name = "document_id") val documentId: String? = null,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,
    val title: String = "",
    val description: String? = null,
    val header: String? = null,
    val body: String? = null,
    val prompt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
