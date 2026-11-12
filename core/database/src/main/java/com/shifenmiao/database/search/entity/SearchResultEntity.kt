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

package com.shifenmiao.database.search.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 搜索结果数据表实体类
 * 用于存储与消息相关的搜索结果
 */
@Entity(
    tableName = "search_results",
    indices = [Index(value = ["message_id"])],
    foreignKeys = [
        ForeignKey(
            entity = com.shifenmiao.database.ai.entity.MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SearchResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 序号
    @ColumnInfo(name = "index")
    val index: Int?,
    
    // 搜索结果URL
    @ColumnInfo(name = "url")
    val url: String?,
    
    // 搜索结果标题
    @ColumnInfo(name = "title")
    val title: String?,
    
    // 关联的消息ID
    @ColumnInfo(name = "message_id")
    val messageId: Int,
    
    // 创建时间
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    // 更新时间
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
